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
    sourceFile: path.join(__dirname, "raw_data/BeeDefinition.java"),
  },
  {
    key: "extrabees",
    name: "ExtraBees",
    parser: parseExtraBees,
    sourceFile: path.join(__dirname, "raw_data/ExtraBeeDefinition.java"),
  },
  {
    key: "careerbees",
    name: "CareerBees",
    parser: parseCareerBees,
    sourceFile: path.join(__dirname, "raw_data/CareerBeeSpecies.java"),
  },
  {
    key: "magicbees",
    name: "MagicBees",
    parser: parseMagicBees,
    sourceFile: path.join(__dirname, "raw_data/EnumBeeSpecies.java"),
  },
  {
    key: "gendustry_color",
    name: "Gendustry Color Bees",
    parser: parseGendustryConfig,
    sourceFile: path.join(__dirname, "raw_data/bees_color.cfg"),
  },
  {
    key: "gendustry_patreon",
    name: "Gendustry Patreon Bees",
    parser: parseGendustryConfig,
    sourceFile: path.join(__dirname, "raw_data/bees_patreon.cfg"),
  },
  {
    key: "meatballcraft",
    name: "MeatballCraft",
    parser: parseGendustryConfig,
    sourceFile: path.join(__dirname, "raw_data/meatball_bees.cfg"),
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
  let maxMutLen = "Mutations".length;
  let maxBranchLen = "Branches".length;
  let maxCombLen = "Combs".length;

  // Totals
  let totalBees = 0;
  let totalMutations = 0;
  let totalBranches = 0;
  let totalCombs = 0;

  for (const config of modsConfig) {
    if (!fs.existsSync(config.sourceFile)) {
      parsedResults.push({ config, error: "Source file not found" });
      continue;
    }

    try {
      const data = config.parser(config.sourceFile);
      data._configName = config.name;
      data._sourceFile = path.basename(config.sourceFile);
      intermediateData.push(data);

      const beeCount = Object.keys(data.bees).length;
      const mutationCount = data.mutations.length;
      const branchCount = Object.keys(data.branches || {}).length;

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
      totalMutations += mutationCount;
      totalBranches += branchCount;
      totalCombs += combCount;

      const displayName = `${config.name} (${data._sourceFile})`;
      maxNameLen = Math.max(maxNameLen, displayName.length);
      maxBeeLen = Math.max(maxBeeLen, String(beeCount).length);
      maxMutLen = Math.max(maxMutLen, String(mutationCount).length);
      maxBranchLen = Math.max(maxBranchLen, String(branchCount).length);
      maxCombLen = Math.max(maxCombLen, String(combCount).length);

      parsedResults.push({
        config,
        data,
        displayName,
        beeCount,
        mutationCount,
        branchCount,
        combCount,
      });
    } catch (error) {
      parsedResults.push({ config, error: error.message });
    }
  }

  // Load manual mutations to add to the table
  const manualMutationsPath = path.join(__dirname, "manual", "mutations.jsonc");
  let manualMutationCount = 0;
  if (fs.existsSync(manualMutationsPath)) {
    const content = fs.readFileSync(manualMutationsPath, "utf-8");
    const jsonContent = content
      .replace(/\/\/.*$/gm, "")
      .replace(/\/\*[\s\S]*?\*\//g, "");
    const manualMutations = JSON.parse(jsonContent);
    manualMutationCount = manualMutations.reduce(
      (sum, group) => sum + group.children.length,
      0
    );
    totalMutations += manualMutationCount;
  }

  // Update max lengths for manual and totals rows
  const manualDisplayName = "Manual (mutations.jsonc)";
  const totalsDisplayName = "TOTAL";
  maxNameLen = Math.max(
    maxNameLen,
    manualDisplayName.length,
    totalsDisplayName.length
  );
  maxMutLen = Math.max(
    maxMutLen,
    String(manualMutationCount).length,
    String(totalMutations).length
  );
  maxBeeLen = Math.max(maxBeeLen, String(totalBees).length);
  maxBranchLen = Math.max(maxBranchLen, String(totalBranches).length);
  maxCombLen = Math.max(maxCombLen, String(totalCombs).length);

  // Print header
  console.log("Parsing source files...\n");
  const header = `${"Source".padEnd(maxNameLen)}  ${"Bees".padStart(
    maxBeeLen
  )}  ${"Mutations".padStart(maxMutLen)}  ${"Branches".padStart(
    maxBranchLen
  )}  ${"Combs".padStart(maxCombLen)}`;
  console.log(header);
  console.log("-".repeat(header.length));

  // Print each row
  for (const result of parsedResults) {
    if (result.error) {
      console.warn(`⚠️  ${result.config.name}: ${result.error}`);
    } else {
      const row = `${result.displayName.padEnd(maxNameLen)}  ${String(
        result.beeCount
      ).padStart(maxBeeLen)}  ${String(result.mutationCount).padStart(
        maxMutLen
      )}  ${String(result.branchCount).padStart(maxBranchLen)}  ${String(
        result.combCount
      ).padStart(maxCombLen)}`;
      console.log(row);
    }
  }

  // Print manual mutations row
  const manualRow = `${manualDisplayName.padEnd(maxNameLen)}  ${"-".padStart(
    maxBeeLen
  )}  ${String(manualMutationCount).padStart(maxMutLen)}  ${"-".padStart(
    maxBranchLen
  )}  ${"-".padStart(maxCombLen)}`;
  console.log(manualRow);

  // Print separator and totals
  console.log("-".repeat(header.length));
  const totalsRow = `${totalsDisplayName.padEnd(maxNameLen)}  ${String(
    totalBees
  ).padStart(maxBeeLen)}  ${String(totalMutations).padStart(
    maxMutLen
  )}  ${String(totalBranches).padStart(maxBranchLen)}  ${String(
    totalCombs
  ).padStart(maxCombLen)}`;
  console.log(totalsRow);

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

    // Display final summary
    console.log("Output files written to data/");
    console.log(`  → bees.jsonc: ${stats.beeCount} bees`);
    console.log(
      `  → mutations.jsonc: ${stats.mutationCount} mutations (${stats.manualMutationCount} manual, ${stats.parsedMutationCount} parsed)`
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
