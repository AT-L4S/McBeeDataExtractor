/**
 * Output Builder
 *
 * Converts intermediate format from all parsers into final JSONC files:
 * - bees.jsonc: All bee species data
 * - mutations.jsonc: All mutation/breeding relationships
 * - combs.jsonc: All honeycomb products
 */

const fs = require("fs");
const path = require("path");

/**
 * Build final JSONC files from intermediate format data
 * @param {Array<Object>} intermediateData - Array of intermediate format objects from parsers
 * @param {string} outputDir - Directory to write JSONC files to
 * @returns {Object} Statistics about the build
 */
function buildOutput(intermediateData, outputDir) {
  // Merge all data from different mods
  const merged = {
    bees: {},
    mutations: [],
    branches: {},
    combs: {},
  };

  intermediateData.forEach((data) => {
    // Merge bees
    Object.assign(merged.bees, data.bees);

    // Merge mutations
    merged.mutations.push(...data.mutations);

    // Merge branches
    Object.assign(merged.branches, data.branches);
  });

  // Load manual mutations as starting template
  const manualMutationsPath = path.join(__dirname, "manual", "mutations.jsonc");
  let manualMutations = [];
  let originalManualMutationCount = 0;
  if (fs.existsSync(manualMutationsPath)) {
    const content = fs.readFileSync(manualMutationsPath, "utf-8");
    // Remove JSONC comments
    const jsonContent = content
      .replace(/\/\/.*$/gm, "")
      .replace(/\/\*[\s\S]*?\*\//g, "");
    manualMutations = JSON.parse(jsonContent);
    // Save original count BEFORE any merges happen
    originalManualMutationCount = manualMutations.reduce((sum, group) => {
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
  }

  // Extract comb information from bee products
  extractCombs(merged);

  // Build bees.jsonc
  const beesOutput = buildBeesJsonc(merged);
  writeJsonc(
    path.join(outputDir, "bees.jsonc"),
    beesOutput,
    "Bee Species Data"
  );

  // Build mutations.jsonc (starting with manual mutations)
  const { output: breedingOutput, stats: mutationStats } =
    buildBreedingPairsJsonc(merged, manualMutations);
  writeJsonc(
    path.join(outputDir, "mutations.jsonc"),
    breedingOutput,
    "Breeding Pairs Data"
  );

  // Build combs.jsonc
  const combsOutput = buildCombsJsonc(merged);
  writeJsonc(
    path.join(outputDir, "combs.jsonc"),
    combsOutput,
    "Honeycomb Data"
  );

  // Calculate total mutations in output (count all mutation paths)
  const totalMutationCount = breedingOutput.reduce((sum, group) => {
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

  return {
    beeCount: Object.keys(beesOutput).length,
    mutationCount: totalMutationCount,
    manualMutationCount: originalManualMutationCount,
    parsedMutationCount: totalMutationCount - originalManualMutationCount,
    combCount: Object.keys(combsOutput).length,
    skippedMutations: mutationStats.skippedMutations,
  };
}

/**
 * Extract comb information from bee products
 */
function extractCombs(merged) {
  Object.entries(merged.bees).forEach(([uid, bee]) => {
    if (bee.products && bee.products.length > 0) {
      bee.products.forEach((product) => {
        if (product.item.includes("comb")) {
          const combId = product.item;
          if (!merged.combs[combId]) {
            merged.combs[combId] = {
              id: combId,
              name: formatCombName(combId),
              producers: [],
            };
          }
          // Use the original UID directly (already in mod:name format, lowercase, no spaces)
          merged.combs[combId].producers.push({
            bee: uid,
            chance: product.chance,
          });
        }
      });
    }
  });
}

/**
 * Format comb name from ID
 */
function formatCombName(combId) {
  const parts = combId.split(".");
  const name = parts[parts.length - 1];
  return name.charAt(0).toUpperCase() + name.slice(1).replace(/_/g, " ");
}

/**
 * Build bees.jsonc content in the format matching existing data
 * Key format: "Mod:BeeName" (e.g., "Forestry:Forest", "ExtraBees:Blue")
 */
function buildBeesJsonc(merged) {
  const output = {};

  // Sort bees by mod, then by name
  const sortedBees = Object.entries(merged.bees).sort((a, b) => {
    const modCompare = a[1].mod.localeCompare(b[1].mod);
    if (modCompare !== 0) return modCompare;
    return a[1].name.localeCompare(b[1].name);
  });

  sortedBees.forEach(([uid, bee]) => {
    // Use the original parser UID as the key (already in mod:name format, lowercase, no spaces/underscores)
    const key = uid;

    // Build bee object matching existing format
    const beeData = {
      mod: bee.mod,
      name: bee.name,
      idealTemperature: bee.temperature || "",
      idealHumidity: bee.humidity || "",
      temperatureTolerance: "", // Not available in parsed data
      humidityTolerance: "", // Not available in parsed data
      speed: "", // Not available in parsed data
      lifespan: "", // Not available in parsed data
      fertility: "", // Not available in parsed data
      neverSleeps: false, // Not available in parsed data
      caveDwelling: false, // Not available in parsed data
      tolerantFlyer: false, // Not available in parsed data
    };

    // Add products if present (convert item format and preserve isSpecialty flag)
    if (bee.products && bee.products.length > 0) {
      beeData.products = bee.products.map((p) => {
        const product = {
          item: p.item,
          chance: p.chance,
        };
        // Only include isSpecialty if it's explicitly true (omit for regular products)
        if (p.isSpecialty === true) {
          product.isSpecialty = true;
        }
        return product;
      });
    } else {
      beeData.products = [];
    }

    // Add additional properties from parsed data (as supplementary info)
    if (bee.branch) beeData.branch = bee.branch;
    if (bee.binomial) beeData.binomial = bee.binomial;
    if (bee.dominant !== undefined) beeData.dominant = bee.dominant;
    if (bee.colors) beeData.colors = bee.colors;
    if (bee.hasEffect !== undefined) beeData.hasEffect = bee.hasEffect;
    if (bee.isSecret !== undefined) beeData.isSecret = bee.isSecret;

    output[key] = beeData;
  });

  return output;
}

/**
 * Build mutations.jsonc content in Option D format
 * Format: Array of {parents: [], children: {species: {chance, mutations: []}}}}
 */
function buildBreedingPairsJsonc(merged, manualMutations = []) {
  // Start with manual mutations as template
  const output = [...manualMutations];
  const skippedMutations = [];

  // Group mutations by parent pair (start with manual mutations)
  const mutationGroups = new Map();

  // Index manual mutations for quick lookup
  const manualMutationSet = new Set();
  manualMutations.forEach((group) => {
    const parentKey = group.parents.sort().join("|");
    Object.entries(group.children).forEach(([species, childData]) => {
      // For each mutation in the species' requirements array
      const requirementsArray = childData.requirements || [];
      requirementsArray.forEach((requirement) => {
        const chance = requirement.chance || childData.chance;
        const mutationKey = `${parentKey}|${species}|${chance}`;
        manualMutationSet.add(mutationKey);
      });
    });
    // Add to mutation groups map - deep copy the children structure
    mutationGroups.set(parentKey, {
      parents: [...group.parents],
      children: JSON.parse(JSON.stringify(group.children)),
    });
  });

  // Track seen mutations to filter duplicates
  const seenMutationKeys = new Set();

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

  merged.mutations.forEach((mutation) => {
    // Check for null/undefined identifiers
    if (!mutation.parent1 || !mutation.parent2 || !mutation.offspring) {
      skippedMutations.push({
        offspring: mutation.offspring,
        inManual: false,
      });
      return;
    }

    // Check if all bees exist in the bees map
    const parent1Bee = merged.bees[mutation.parent1];
    const parent2Bee = merged.bees[mutation.parent2];
    const offspringBee = merged.bees[mutation.offspring];

    if (!parent1Bee || !parent2Bee || !offspringBee) {
      // Check if this mutation is in manual/mutations.jsonc
      const isInManual = checkIfInManualMutations(
        mutation,
        manualMutationSet,
        merged.bees
      );

      skippedMutations.push({
        offspring: mutation.offspring,
        inManual: isInManual,
      });

      return;
    }

    // Use the UIDs directly from the mutation data - they are already in the correct format
    const parent1 = mutation.parent1;
    const parent2 = mutation.parent2;
    const offspring = mutation.offspring;

    // Create sorted key for parent pair (so [A,B] and [B,A] are treated the same)
    const parentKey = [parent1, parent2].sort().join("|");

    // Check if this exact mutation already exists in manual mutations
    // Include serialized conditions to distinguish mutations with different requirements
    const conditionsKey = serializeConditions(mutation.conditions);
    const mutationKey = `${parentKey}|${offspring}|${
      mutation.chance / 100
    }${conditionsKey}`;

    // For manual mutation check, use key without conditions (manual mutations don't have conditions key)
    const baseKey = `${parentKey}|${offspring}|${mutation.chance / 100}`;
    if (manualMutationSet.has(baseKey)) {
      // Skip - already in manual mutations
      skippedMutations.push({
        offspring: mutation.offspring,
        inManual: true,
      });
      return;
    }

    // Skip duplicates (same mutation from multiple sources or duplicates within a mod)
    if (seenMutationKeys.has(mutationKey)) {
      return;
    }
    seenMutationKeys.add(mutationKey);

    if (!mutationGroups.has(parentKey)) {
      mutationGroups.set(parentKey, {
        parents: [parent1, parent2].sort(),
        children: {},
      });
    }

    // Get or create the offspring entry
    const group = mutationGroups.get(parentKey);
    if (!group.children[offspring]) {
      group.children[offspring] = {
        chance: mutation.chance / 100, // default chance
        requirements: [],
      };
    }

    // Build mutation entry (requirements + optional chance override)
    const mutationEntry = {};

    // Add requirements if present
    if (mutation.conditions && Object.keys(mutation.conditions).length > 0) {
      // Temperature restrictions
      if (mutation.conditions.temperature) {
        mutationEntry.temperature = mutation.conditions.temperature;
      }

      // Humidity restrictions
      if (mutation.conditions.humidity) {
        mutationEntry.humidity = mutation.conditions.humidity;
      }

      // Biome restrictions
      if (mutation.conditions.biome) {
        mutationEntry.biome = mutation.conditions.biome;
      }

      // Date range (seasonal bees)
      if (mutation.conditions.dateRange) {
        mutationEntry.dateRange = mutation.conditions.dateRange;
      }

      // Time of day requirement
      if (mutation.conditions.timeOfDay) {
        mutationEntry.timeOfDay = mutation.conditions.timeOfDay;
      }

      // Required block (check both property names for compatibility)
      if (mutation.conditions.block) {
        mutationEntry.block = mutation.conditions.block;
      } else if (mutation.conditions.requiredBlock) {
        mutationEntry.block = mutation.conditions.requiredBlock;
      }

      // Moon phase (MagicBees)
      if (mutation.conditions.moonPhase) {
        mutationEntry.moonPhase = mutation.conditions.moonPhase;
      }

      // Moon phase bonus multiplier (MagicBees)
      if (mutation.conditions.moonPhaseBonus) {
        mutationEntry.moonPhaseBonus = mutation.conditions.moonPhaseBonus;
      }

      // Thaumcraft vis requirement (MagicBees)
      if (mutation.conditions.thaumcraftVis) {
        mutationEntry.thaumcraftVis = mutation.conditions.thaumcraftVis;
      }

      // Recent explosion requirement (CareerBees)
      if (mutation.conditions.requireExplosion) {
        mutationEntry.requireExplosion = true;
      }

      // Player name requirement (ExtraBees easter egg)
      if (mutation.conditions.requirePlayer) {
        mutationEntry.requirePlayer = mutation.conditions.requirePlayer;
      }

      // Dimension requirement
      if (mutation.conditions.dimension) {
        mutationEntry.dimension = mutation.conditions.dimension;
      }

      // Secret mutation flag
      if (mutation.conditions.isSecret) {
        mutationEntry.isSecret = true;
      }
    }

    // Check if this mutation has a different chance than the default
    const defaultChance = group.children[offspring].chance;
    if (Math.abs(mutation.chance / 100 - defaultChance) > 0.0001) {
      mutationEntry.chance = mutation.chance / 100;
    }

    // Only add the requirement if it has properties (not empty)
    if (Object.keys(mutationEntry).length > 0) {
      group.children[offspring].requirements.push(mutationEntry);
    }
  });

  // Update manual groups in output with any new children that were added
  output.forEach((group) => {
    const parentKey = group.parents.sort().join("|");
    const updatedGroup = mutationGroups.get(parentKey);
    if (updatedGroup) {
      // Merge new species or requirements into existing children
      Object.entries(updatedGroup.children).forEach(([species, childData]) => {
        if (!group.children[species]) {
          // New species - add it (omit empty requirements array)
          if (childData.requirements && childData.requirements.length === 0) {
            group.children[species] = { chance: childData.chance };
          } else {
            group.children[species] = childData;
          }
        } else {
          // Existing species - merge requirements
          const requirements = childData.requirements || [];
          requirements.forEach((newRequirement) => {
            // Check if this requirement already exists
            const exists = (group.children[species].requirements || []).some(
              (existing) =>
                JSON.stringify(existing) === JSON.stringify(newRequirement)
            );
            if (!exists) {
              if (!group.children[species].requirements) {
                group.children[species].requirements = [];
              }
              group.children[species].requirements.push(newRequirement);
            }
          });
        }
      });
    }
  });

  // Convert map to array (only new entries not in manual mutations)
  const newGroups = Array.from(mutationGroups.values()).filter((group) => {
    const parentKey = group.parents.sort().join("|");
    // Check if this group was in the original manual mutations
    const wasManual = manualMutations.some(
      (manual) => manual.parents.sort().join("|") === parentKey
    );
    return !wasManual;
  });

  // Sort new groups
  const sortedGroups = newGroups.sort((a, b) => {
    // Sort by first parent, then by second parent
    const parent1Compare = a.parents[0].localeCompare(b.parents[0]);
    if (parent1Compare !== 0) return parent1Compare;
    return a.parents[1].localeCompare(b.parents[1]);
  });

  sortedGroups.forEach((group) => {
    output.push(group);
  });

  // Sort children object keys alphabetically and clean up empty requirements for each group
  output.forEach((group) => {
    const sortedChildren = {};
    Object.keys(group.children)
      .sort()
      .forEach((key) => {
        const child = group.children[key];
        // Remove empty requirements array
        if (child.requirements && child.requirements.length === 0) {
          delete child.requirements;
        }
        sortedChildren[key] = child;
      });
    group.children = sortedChildren;
  });

  return {
    output,
    stats: {
      skippedMutations,
    },
  };
}

/**
 * Check if a mutation is already in manual/mutations.jsonc
 */
function checkIfInManualMutations(mutation, manualMutationSet, beesMap) {
  // Identifier is already in mod:name format, just normalize case and spaces
  const tryResolve = (identifier) => {
    if (!identifier) return null;

    // Already in mod:name format, just ensure lowercase and no spaces
    if (identifier.includes(":")) {
      const [mod, name] = identifier.split(":");
      return `${mod.toLowerCase()}:${name
        .toLowerCase()
        .replace(/[_\s]+/g, "")}`;
    }

    return null;
  };

  const offspring = tryResolve(mutation.offspring);

  // If we can't resolve the offspring, it's not in manual mutations
  if (!offspring) {
    return false;
  }

  // Check if ANY manual mutation produces this offspring (regardless of parents or chance)
  for (const key of manualMutationSet) {
    const parts = key.split("|");
    if (parts.length >= 3) {
      // Key format: parent1|parent2|offspring|chance
      const manualOffspring = parts[2];

      // If this offspring is defined in manual mutations, it's already covered
      if (manualOffspring === offspring) {
        return true;
      }
    }
  }

  return false;
}

/**
 * Build combs.jsonc content
 */
function buildCombsJsonc(merged) {
  const output = {};

  // Sort combs by ID
  const sortedCombs = Object.entries(merged.combs).sort((a, b) =>
    a[0].localeCompare(b[0])
  );

  sortedCombs.forEach(([id, comb]) => {
    output[id] = {
      name: comb.name,
      producers: comb.producers.sort((a, b) => a.bee.localeCompare(b.bee)),
    };
  });

  return output;
}

/**
 * Write JSONC file with header comment
 */
function writeJsonc(filePath, data, description) {
  const header = `// ${description}\n// Generated from mod source files\n// Do not edit manually - regenerate using scripts/build.js\n\n`;

  // Use custom formatter for mutations.jsonc to match example format
  let jsonContent;
  if (filePath.endsWith("mutations.jsonc")) {
    jsonContent = formatMutationsJson(data);
  } else {
    jsonContent = JSON.stringify(data, null, 2);
  }

  fs.writeFileSync(filePath, header + jsonContent);
}

/**
 * Custom JSON formatter for mutations that:
 * - Keeps arrays of primitives inline
 * - Keeps arrays of objects multi-line
 * - Adds blank lines between mutation groups
 */
function formatMutationsJson(data, indent = 0) {
  const indentStr = "  ".repeat(indent);
  const nextIndent = "  ".repeat(indent + 1);

  if (Array.isArray(data)) {
    if (data.length === 0) return "[]";

    // Check if array contains only primitives
    const allPrimitives = data.every(
      (item) =>
        typeof item === "string" ||
        typeof item === "number" ||
        typeof item === "boolean" ||
        item === null
    );

    if (allPrimitives) {
      // Format inline for primitive arrays
      return "[" + data.map((item) => JSON.stringify(item)).join(", ") + "]";
    } else {
      // Format multi-line for object arrays
      const items = data.map((item, index) => {
        const formattedItem = formatMutationsJson(item, indent + 1);
        const comma = index < data.length - 1 ? "," : "";
        return nextIndent + formattedItem + comma;
      });
      return "[\n" + items.join("\n") + "\n" + indentStr + "]";
    }
  } else if (typeof data === "object" && data !== null) {
    const keys = Object.keys(data);
    if (keys.length === 0) return "{}";

    const items = keys.map((key) => {
      const value = data[key];
      const formattedValue = formatMutationsJson(value, indent + 1);
      return `${nextIndent}"${key}": ${formattedValue}`;
    });

    return "{\n" + items.join(",\n") + "\n" + indentStr + "}";
  } else {
    return JSON.stringify(data);
  }
}

/**
 * Main export function
 */
function buildOutputFiles(intermediateData, outputDir = "./data") {
  // Ensure output directory exists
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  return buildOutput(intermediateData, outputDir);
}

module.exports = { buildOutputFiles };

// CLI usage
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 1) {
    console.log(
      "Usage: node output_builder.js <intermediate-json-file1> [intermediate-json-file2] [...] [--output-dir <dir>]"
    );
    console.log("");
    console.log("Example:");
    console.log(
      "  node output_builder.js forestry.json extrabees.json --output-dir ./data"
    );
    process.exit(1);
  }

  // Parse arguments
  let outputDir = "./data";
  const inputFiles = [];

  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--output-dir" && i + 1 < args.length) {
      outputDir = args[i + 1];
      i++;
    } else {
      inputFiles.push(args[i]);
    }
  }

  // Load intermediate data
  const intermediateData = inputFiles.map((file) => {
    console.log(`Loading ${file}...`);
    return JSON.parse(fs.readFileSync(file, "utf-8"));
  });

  buildOutputFiles(intermediateData, outputDir);
}
