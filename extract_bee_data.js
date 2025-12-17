/**
 * Main Build Script
 *
 * Orchestrates all parsers to convert mod source files into final JSONC data files.
 * Reads mod source locations, runs appropriate parsers, and builds output files.
 */

const fs = require("fs");
const path = require("path");

// Import parsers
const { parseForestry } = require("./parsers/forestry_parser");
const { parseExtraBees } = require("./parsers/extrabees_parser");
const { parseCareerBees } = require("./parsers/careerbees_parser");
const { parseMagicBees } = require("./parsers/magicbees_parser");
const { parseGendustryConfig } = require("./parsers/gendustry_config_parser");
const { buildOutputFiles } = require("./output_builder");
const {
  buildShortestPathMutations,
  writeShortestMutationsJsonc,
} = require("./shortest_path_builder");

/**
 * Configuration for mod parsers
 * Ordered array: Forestry first (base mod), then addon mods, then Gendustry
 * Update these paths based on MOD_SOURCE_LOCATIONS.md
 */
const MOD_CONFIGS = [
  {
    key: "forestry",
    name: "Forestry",
    parser: parseForestry,
    sourceFile: path.join(__dirname, "raw_data/forestry/BeeDefinition.java"),
    langFile: path.join(__dirname, "raw_data/forestry/lang/en_us.lang"),
  },
  {
    key: "extrabees",
    name: "ExtraBees",
    parser: parseExtraBees,
    sourceFile: path.join(
      __dirname,
      "raw_data/extrabees/ExtraBeeDefinition.java"
    ),
    langFile: path.join(__dirname, "raw_data/extrabees/lang/en_US.lang"),
  },
  {
    key: "careerbees",
    name: "CareerBees",
    parser: parseCareerBees,
    sourceFile: path.join(
      __dirname,
      "raw_data/careerbees/CareerBeeSpecies.java"
    ),
    langFile: path.join(__dirname, "raw_data/careerbees/lang/en_us.lang"),
  },
  {
    key: "magicbees",
    name: "MagicBees",
    parser: parseMagicBees,
    sourceFile: path.join(__dirname, "raw_data/magicbees/EnumBeeSpecies.java"),
    langFile: path.join(__dirname, "raw_data/magicbees/lang/en_US.lang"),
  },
  {
    key: "gendustry_color",
    name: "Gendustry Color Bees",
    parser: parseGendustryConfig,
    sourceFile: path.join(__dirname, "raw_data/gendustry/bees_color.cfg"),
    langFile: path.join(__dirname, "raw_data/gendustry/lang/en_US.lang"),
  },
  {
    key: "gendustry_patreon",
    name: "Gendustry Patreon Bees",
    parser: parseGendustryConfig,
    sourceFile: path.join(__dirname, "raw_data/gendustry/bees_patreon.cfg"),
    langFile: path.join(__dirname, "raw_data/gendustry/lang/en_US.lang"),
  },
  {
    key: "meatballcraft",
    name: "MeatballCraft",
    parser: parseGendustryConfig,
    sourceFile: path.join(__dirname, "raw_data/meatball_bees.cfg"),
    // MeatballCraft uses Gendustry's lang file for built-in bees, but custom bees don't have lang entries
    langFile: path.join(__dirname, "raw_data/gendustry/lang/en_US.lang"),
  },
];

/**
 * Parse all mods and collect intermediate data
 */
function parseAllMods(modsToInclude = null) {
  const intermediateData = [];
  const modsConfig = modsToInclude
    ? MOD_CONFIGS.filter((config) => modsToInclude.includes(config.key))
    : MOD_CONFIGS;

  // First pass: collect all data and find max widths for alignment
  const parsedResults = [];
  let maxNameLen = "Source".length;
  let maxBeeLen = "Bees".length;
  let maxSuccessLen = "Success".length;
  let maxSkipLen = "Skipped".length;
  let maxBranchLen = "Branches".length;
  let maxCombLen = "Combs".length;

  // Totals
  let totalBees = 0;
  let totalSuccessMutations = 0;
  let totalSkippedMutations = 0;
  let totalDuplicateMutations = 0;
  let totalMergedMutations = 0;
  let totalBranches = 0;
  let totalCombs = 0;

  // Load manual mutations first to check for skipped mutations
  const manualMutationsPath = path.join(__dirname, "manual", "mutations.jsonc");
  let manualMutations = [];
  let manualMutationCount = 0;
  const manualMutationSet = new Set();
  const manualParentPairs = new Set();

  if (fs.existsSync(manualMutationsPath)) {
    const content = fs.readFileSync(manualMutationsPath, "utf-8");
    const jsonContent = content
      .replace(/\/\/.*$/gm, "")
      .replace(/\/\*[\s\S]*?\*\//g, "");
    manualMutations = JSON.parse(jsonContent);
    manualMutationCount = manualMutations.reduce((sum, group) => {
      return (
        sum +
        Object.values(group.children).reduce((childSum, childData) => {
          // Count requirements, or 1 if no requirements array (unconditional mutation)
          return (
            childSum +
            (childData.requirements ? childData.requirements.length : 1)
          );
        }, 0)
      );
    }, 0);
    // Build set of exact mutation keys (parentKey|offspring|chance) and parent pairs
    manualMutations.forEach((group) => {
      const parentKey = group.parents.sort().join("|");
      manualParentPairs.add(parentKey);
      Object.entries(group.children).forEach(([species, childData]) => {
        const requirementsArray = childData.requirements || [];
        requirementsArray.forEach((requirement) => {
          const chance = requirement.chance || childData.chance;
          const mutationKey = `${parentKey}|${species}|${chance}`;
          manualMutationSet.add(mutationKey);
        });
      });
    });
  }

  // First pass: collect all bees to build the lookup map
  const allBees = {};
  for (const config of modsConfig) {
    if (!fs.existsSync(config.sourceFile)) continue;
    try {
      // Pass lang file to parser if it exists
      const langFile =
        config.langFile && fs.existsSync(config.langFile)
          ? config.langFile
          : null;

      // Handle different parser signatures
      let data;
      if (
        config.key.startsWith("gendustry") ||
        config.key === "meatballcraft"
      ) {
        // Gendustry parser signature: (configPath, modName, langFilePath)
        data = config.parser(config.sourceFile, config.name, langFile);
      } else {
        // Other parsers signature: (sourceFile, langFilePath)
        data = config.parser(config.sourceFile, langFile);
      }
      Object.assign(allBees, data.bees);
    } catch (error) {
      // Ignore errors in first pass
    }
  }

  // Track all mutations for duplicate detection
  const seenMutations = new Set();

  // Helper function to serialize conditions for mutation key generation
  const serializeConditions = (conditions) => {
    if (!conditions || Object.keys(conditions).length === 0) return "";
    // Create a stable, sorted string representation of conditions
    const sortedKeys = Object.keys(conditions).sort();
    const parts = sortedKeys.map((key) => {
      const value = conditions[key];
      if (Array.isArray(value)) {
        return `${key}=${value.slice().sort().join(",")}`;
      }
      return `${key}=${JSON.stringify(value)}`;
    });
    return "|" + parts.join("|");
  };

  // Second pass: collect data with mutation success/skip counts
  for (const config of modsConfig) {
    if (!fs.existsSync(config.sourceFile)) {
      parsedResults.push({ config, error: "Source file not found" });
      continue;
    }

    try {
      // Pass lang file to parser if it exists
      const langFile =
        config.langFile && fs.existsSync(config.langFile)
          ? config.langFile
          : null;

      // Handle different parser signatures
      let data;
      if (
        config.key.startsWith("gendustry") ||
        config.key === "meatballcraft"
      ) {
        // Gendustry parser signature: (configPath, modName, langFilePath)
        data = config.parser(config.sourceFile, config.name, langFile);
      } else {
        // Other parsers signature: (sourceFile, langFilePath)
        data = config.parser(config.sourceFile, langFile);
      }
      data._configName = config.name;
      data._sourceFile = path.basename(config.sourceFile);
      intermediateData.push(data);

      const beeCount = Object.keys(data.bees).length;
      const branchCount = Object.keys(data.branches || {}).length;

      // Count successful, skipped (unresolvable), duplicate, and merged mutations
      let successMutations = 0;
      let skippedMutations = 0;
      let duplicateMutations = 0;
      let mergedMutations = 0;

      data.mutations.forEach((mutation) => {
        const parent1Bee = allBees[mutation.parent1];
        const parent2Bee = allBees[mutation.parent2];
        const offspringBee = allBees[mutation.offspring];

        if (!parent1Bee || !parent2Bee || !offspringBee) {
          skippedMutations++;
        } else {
          // Build mutation key matching output_builder.js format
          const parent1Key = `${parent1Bee.mod.toLowerCase()}:${parent1Bee.name
            .toLowerCase()
            .replace(/\s+/g, "")}`;
          const parent2Key = `${parent2Bee.mod.toLowerCase()}:${parent2Bee.name
            .toLowerCase()
            .replace(/\s+/g, "")}`;
          const offspringKey = `${offspringBee.mod.toLowerCase()}:${offspringBee.name
            .toLowerCase()
            .replace(/\s+/g, "")}`;
          const parentKey = [parent1Key, parent2Key].sort().join("|");
          // Note: output_builder divides chance by 100, so we do the same
          // Include serialized conditions to distinguish mutations with different requirements
          const conditionsKey = serializeConditions(mutation.conditions);
          const mutationKey = `${parentKey}|${offspringKey}|${
            mutation.chance / 100
          }${conditionsKey}`;

          // For manual mutation check, use key without conditions (manual mutations don't have conditions key)
          const baseKey = `${parentKey}|${offspringKey}|${
            mutation.chance / 100
          }`;

          // Check if exact mutation exists in manual mutations or is a duplicate
          if (manualMutationSet.has(baseKey)) {
            duplicateMutations++;
          } else if (seenMutations.has(mutationKey)) {
            duplicateMutations++;
          } else if (manualParentPairs.has(parentKey)) {
            // Mutation shares parent pair with manual - will be merged into manual group
            // These still count as success since they're added to output
            mergedMutations++;
            seenMutations.add(mutationKey);
            successMutations++;
          } else {
            seenMutations.add(mutationKey);
            successMutations++;
          }
        }
      });

      // Count combs from bee products
      const combSet = new Set();
      Object.values(data.bees).forEach((bee) => {
        if (bee.products) {
          bee.products.forEach((p) => {
            if (p.item && p.item.toLowerCase().includes("comb")) {
              combSet.add(p.item);
            }
          });
        }
      });
      const combCount = combSet.size;

      totalBees += beeCount;
      totalSuccessMutations += successMutations;
      totalSkippedMutations += skippedMutations;
      totalDuplicateMutations += duplicateMutations;
      totalMergedMutations += mergedMutations;
      totalBranches += branchCount;
      totalCombs += combCount;

      const displayName = `${config.name} (${data._sourceFile})`;
      maxNameLen = Math.max(maxNameLen, displayName.length);
      maxBeeLen = Math.max(maxBeeLen, String(beeCount).length);
      maxSuccessLen = Math.max(maxSuccessLen, String(successMutations).length);
      maxSkipLen = Math.max(maxSkipLen, String(skippedMutations).length);
      maxBranchLen = Math.max(maxBranchLen, String(branchCount).length);
      maxCombLen = Math.max(maxCombLen, String(combCount).length);

      parsedResults.push({
        config,
        data,
        displayName,
        beeCount,
        successMutations,
        skippedMutations,
        duplicateMutations,
        mergedMutations,
        branchCount,
        combCount,
      });
    } catch (error) {
      parsedResults.push({ config, error: error.message });
    }
  }

  // Update max lengths for manual and totals rows
  const manualDisplayName = "Manual (mutations.jsonc)";
  const totalsDisplayName = "TOTAL";
  maxNameLen = Math.max(
    maxNameLen,
    manualDisplayName.length,
    totalsDisplayName.length
  );
  maxSuccessLen = Math.max(
    maxSuccessLen,
    String(manualMutationCount).length,
    String(totalSuccessMutations + manualMutationCount).length
  );
  maxBeeLen = Math.max(maxBeeLen, String(totalBees).length);
  maxBranchLen = Math.max(maxBranchLen, String(totalBranches).length);
  maxCombLen = Math.max(maxCombLen, String(totalCombs).length);

  // Calculate column widths for the mutation header
  const mutationColWidth = maxSuccessLen + maxSkipLen + 3; // 3 for "  " separator

  // Print header (two lines)
  console.log("Parsing source files...\n");

  // First header line with "Mutations" spanning two columns
  const mutationsLabel = "Mutations";
  const mutationsHeaderPad = Math.floor(
    (mutationColWidth - mutationsLabel.length) / 2
  );
  const headerLine1 = `${"".padEnd(maxNameLen)}  ${"".padStart(
    maxBeeLen
  )}  ${" ".repeat(mutationsHeaderPad)}${mutationsLabel}${"".padEnd(
    mutationColWidth - mutationsHeaderPad - mutationsLabel.length
  )}  ${"".padStart(maxBranchLen)}  ${"".padStart(maxCombLen)}`;

  // Second header line with actual column names
  const headerLine2 = `${"Source".padEnd(maxNameLen)}  ${"Bees".padStart(
    maxBeeLen
  )}  ${"Success".padStart(maxSuccessLen)}  ${"Skipped".padStart(
    maxSkipLen
  )}  ${"Branches".padStart(maxBranchLen)}  ${"Combs".padStart(maxCombLen)}`;

  console.log(headerLine1);
  console.log(headerLine2);
  console.log("-".repeat(headerLine2.length));

  // Print each row
  for (const result of parsedResults) {
    if (result.error) {
      console.warn(`⚠️  ${result.config.name}: ${result.error}`);
    } else {
      const row = `${result.displayName.padEnd(maxNameLen)}  ${String(
        result.beeCount
      ).padStart(maxBeeLen)}  ${String(result.successMutations).padStart(
        maxSuccessLen
      )}  ${String(result.skippedMutations).padStart(maxSkipLen)}  ${String(
        result.branchCount
      ).padStart(maxBranchLen)}  ${String(result.combCount).padStart(
        maxCombLen
      )}`;
      console.log(row);
    }
  }

  // Print manual mutations row
  const manualRow = `${manualDisplayName.padEnd(maxNameLen)}  ${"-".padStart(
    maxBeeLen
  )}  ${String(manualMutationCount).padStart(maxSuccessLen)}  ${"-".padStart(
    maxSkipLen
  )}  ${"-".padStart(maxBranchLen)}  ${"-".padStart(maxCombLen)}`;
  console.log(manualRow);

  // Print separator and totals
  console.log("-".repeat(headerLine2.length));
  const totalMutations = totalSuccessMutations + manualMutationCount;
  const totalsRow = `${totalsDisplayName.padEnd(maxNameLen)}  ${String(
    totalBees
  ).padStart(maxBeeLen)}  ${String(totalMutations).padStart(
    maxSuccessLen
  )}  ${String(totalSkippedMutations).padStart(maxSkipLen)}  ${String(
    totalBranches
  ).padStart(maxBranchLen)}  ${String(totalCombs).padStart(maxCombLen)}`;
  console.log(totalsRow);

  // Print summary notes
  const notes = [];
  if (totalDuplicateMutations > 0) {
    notes.push(`${totalDuplicateMutations} duplicates filtered`);
  }
  if (totalMergedMutations > 0) {
    notes.push(`${totalMergedMutations} merged into manual groups`);
  }
  if (notes.length > 0) {
    console.log(`\n(${notes.join(", ")})`);
  }

  console.log("");
  return intermediateData;
}

/**
 * Main build function
 */
function build(options = {}) {
  const {
    modsToInclude = null,
    outputDir = path.join(__dirname, "data"),
    saveIntermediate = false,
    intermediateDir = path.join(__dirname, "intermediate"),
  } = options;

  try {
    // Parse all mods
    const intermediateData = parseAllMods(modsToInclude);

    if (intermediateData.length === 0) {
      console.error("No mods were successfully parsed. Exiting.");
      process.exit(1);
    }

    // Save intermediate files if requested
    if (saveIntermediate) {
      if (!fs.existsSync(intermediateDir)) {
        fs.mkdirSync(intermediateDir, { recursive: true });
      }

      intermediateData.forEach((data, index) => {
        const modName = Object.values(data.bees)[0]?.mod || `mod_${index}`;
        const filename = `${modName.toLowerCase()}_intermediate.json`;
        const filepath = path.join(intermediateDir, filename);
        fs.writeFileSync(filepath, JSON.stringify(data, null, 2));
      });
    }

    // Build output files and get stats
    const stats = buildOutputFiles(intermediateData, outputDir);

    // Build shortest path mutations
    console.log("\nCalculating shortest breeding paths...");
    const mutationsPath = path.join(outputDir, "mutations.jsonc");
    const mutationsContent = fs.readFileSync(mutationsPath, "utf-8");
    const mutationsJson = mutationsContent
      .replace(/\/\/.*$/gm, "")
      .replace(/\/\*[\s\S]*?\*\//g, "");
    const allMutations = JSON.parse(mutationsJson);

    // Get all bees from intermediate data
    const allBees = {};
    intermediateData.forEach((data) => {
      Object.assign(allBees, data.bees);
    });

    const shortestMutations = buildShortestPathMutations(allBees, allMutations);
    const shortestMutationsPath = path.join(
      outputDir,
      "shortest_mutations.jsonc"
    );
    writeShortestMutationsJsonc(shortestMutationsPath, shortestMutations);

    // Count shortest mutations
    const shortestMutationCount = shortestMutations.reduce((sum, group) => {
      return (
        sum +
        Object.values(group.children).reduce((childSum, childData) => {
          return (
            childSum +
            (childData.requirements ? childData.requirements.length : 1)
          );
        }, 0)
      );
    }, 0);

    // Display final summary
    console.log("\nOutput files written to data/");
    console.log(`  → bees.jsonc: ${stats.beeCount} bees`);
    console.log(
      `  → mutations.jsonc: ${stats.mutationCount} mutations (${stats.manualMutationCount} manual, ${stats.parsedMutationCount} parsed)`
    );
    console.log(
      `  → shortest_mutations.jsonc: ${shortestMutationCount} mutations (shortest breeding paths)`
    );
    console.log(`  → combs.jsonc: ${stats.combCount} combs`);

    if (stats.skippedMutations.length > 0) {
      console.log(`\nSkipped ${stats.skippedMutations.length} mutations:`);
      stats.skippedMutations.forEach((skip) => {
        if (skip.inManual) {
          console.log(`  ℹ️  ${skip.offspring} - in manual/mutations.jsonc`);
        } else {
          console.log(`  ⚠️  ${skip.offspring} - species not found`);
        }
      });
    }
  } catch (error) {
    console.error(`✗ Build failed: ${error.message}`);
    process.exit(1);
  }
}

// CLI usage
if (require.main === module) {
  const args = process.argv.slice(2);

  // Parse command line arguments
  const options = {
    modsToInclude: null,
    outputDir: path.join(__dirname, "data"),
    saveIntermediate: false,
    intermediateDir: path.join(__dirname, "intermediate"),
  };

  for (let i = 0; i < args.length; i++) {
    switch (args[i]) {
      case "--help":
      case "-h":
        console.log("Usage: node build.js [options]");
        console.log("");
        console.log("Options:");
        console.log(
          "  --mods <mod1,mod2,...>     Only build specific mods (forestry,extrabees,careerbees,magicbees,meatballcraft)"
        );
        console.log(
          "  --output-dir <dir>         Output directory for JSONC files (default: scripts/data)"
        );
        console.log(
          "  --save-intermediate        Save intermediate JSON files"
        );
        console.log(
          "  --intermediate-dir <dir>   Directory for intermediate files (default: ./scripts/intermediate)"
        );
        console.log("  --help, -h                 Show this help message");
        console.log("");
        console.log("Examples:");
        console.log("  node build.js");
        console.log("  node build.js --mods forestry,extrabees");
        console.log("  node build.js --save-intermediate");
        console.log(
          "  node build.js --output-dir ./output --save-intermediate"
        );
        process.exit(0);

      case "--mods":
        if (i + 1 < args.length) {
          options.modsToInclude = args[i + 1].split(",").map((m) => m.trim());
          i++;
        }
        break;

      case "--output-dir":
        if (i + 1 < args.length) {
          options.outputDir = args[i + 1];
          i++;
        }
        break;

      case "--save-intermediate":
        options.saveIntermediate = true;
        break;

      case "--intermediate-dir":
        if (i + 1 < args.length) {
          options.intermediateDir = args[i + 1];
          i++;
        }
        break;
    }
  }

  build(options);
}

module.exports = { build, parseAllMods, MOD_CONFIGS };
