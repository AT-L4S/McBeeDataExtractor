/**
 * MagicBees EnumBeeSpecies.java Parser
 *
 * Parses MagicBees' EnumBeeSpecies.java file to extract bee species,
 * mutations, and branch information into the intermediate JSON format.
 *
 * MagicBees uses an enum-based pattern similar to Forestry and ExtraBees.
 */

const fs = require("fs");
const path = require("path");

/**
 * Remove comments from Java content
 */
function removeComments(content) {
  // Remove multi-line comments /* */
  content = content.replace(/\/\*[\s\S]*?\*\//g, "");
  // Remove single-line comments //
  content = content.replace(/\/\/.*$/gm, "");
  return content;
}

/**
 * Parse MagicBees EnumBeeSpecies.java file
 * @param {string} filePath - Path to EnumBeeSpecies.java
 * @returns {Object} Intermediate format object with bees, mutations, and branches
 */
function parseMagicBeesSpecies(filePath) {
  let content = fs.readFileSync(filePath, "utf-8");
  content = removeComments(content);

  const result = {
    bees: {},
    mutations: [],
    branches: {},
  };

  // Extract enum constants with their full bodies including registerMutations()
  // Pattern: ENUMNAME("binomial", EnumBeeBranches.BRANCH, dominant, new Color(0xHEX)) { ... },
  const enumPattern =
    /(\w+)\s*\(\s*"([^"]+)"\s*,\s*EnumBeeBranches\.(\w+)\s*,\s*(true|false)\s*,\s*new\s+Color\s*\(\s*0x([0-9A-Fa-f]+)\s*\)(?:\s*,\s*new\s+Color\s*\(\s*0x([0-9A-Fa-f]+)\s*\))?\s*\)\s*\{([\s\S]*?)(?=\n\s{4}\w+\s*\(|;\s*$)/g;

  let match;
  while ((match = enumPattern.exec(content)) !== null) {
    const [
      ,
      enumName,
      binomial,
      branch,
      dominant,
      primaryColor,
      secondaryColor,
      body,
    ] = match;

    // Create UID directly from enum name (lowercase, no underscores)
    // This ensures consistent IDs regardless of display name formatting
    // e.g., "AE_SKYSTONE" → "magicbees:aeskystone"
    const uid = `magicbees:${enumName.toLowerCase().replace(/_/g, "")}`;

    // Create display name for the name field (will be overridden by lang file if available)
    const displayName = enumName
      .split("_")
      .map((part) => {
        // Keep all-caps 2-letter prefixes (TE, AE) as-is
        if (part.length === 2 && /^[A-Z]{2}$/.test(part)) {
          return part;
        }
        return part.charAt(0).toUpperCase() + part.slice(1).toLowerCase();
      })
      .join(" ");

    // Calculate line number where this enum body starts
    const linesBeforeMatch = content
      .substring(0, match.index)
      .split("\n").length;

    const bee = {
      mod: "MagicBees",
      name: displayName,
      binomial: binomial,
      branch: `magicbees.${branch.toLowerCase()}`,
      dominant: dominant === "true",
      colors: {
        primary: `#${primaryColor.toUpperCase()}`,
        secondary: secondaryColor
          ? `#${secondaryColor.toUpperCase()}`
          : `#${primaryColor.toUpperCase()}`,
      },
      temperature: "NORMAL",
      humidity: "NORMAL",
      hasEffect: false,
      isSecret: false,
      products: [],
    };

    // Parse bee body for details
    const bodyDetails = parseBeeBody(body);
    if (bodyDetails.temperature) bee.temperature = bodyDetails.temperature;
    if (bodyDetails.humidity) bee.humidity = bodyDetails.humidity;
    if (bodyDetails.hasEffect) bee.hasEffect = bodyDetails.hasEffect;
    if (bodyDetails.isSecret) bee.isSecret = bodyDetails.isSecret;
    if (bodyDetails.products.length > 0) bee.products = bodyDetails.products;

    result.bees[uid] = bee;

    // Store enum name for mutation parsing
    result.bees[uid]._enumName = enumName;

    // Parse mutations from this bee's registerMutations() method
    const beeMutations = parseBeeMutations(
      body,
      enumName,
      result.bees,
      filePath,
      linesBeforeMatch
    );
    result.mutations.push(...beeMutations);

    // Check if this bee calls registerMundaneMutations()
    if (body.includes("registerMundaneMutations()")) {
      const mundaneMutations = createMundaneMutations(
        uid,
        filePath,
        linesBeforeMatch
      );
      result.mutations.push(...mundaneMutations);
    }
  }

  // Extract branch names
  const branches = new Set();
  Object.values(result.bees).forEach((bee) => {
    if (bee.branch) {
      branches.add(bee.branch);
    }
  });

  branches.forEach((branchUID) => {
    const parts = branchUID.split(".");
    const name = parts[parts.length - 1];
    result.branches[branchUID] = {
      name: name.charAt(0).toUpperCase() + name.slice(1),
      scientific: name,
    };
  });

  // Clean up temporary fields
  Object.values(result.bees).forEach((bee) => {
    delete bee._enumName;
  });

  return result;
}

/**
 * Parse bee body for additional details
 */
function parseBeeBody(body) {
  const details = {
    temperature: null,
    humidity: null,
    hasEffect: false,
    isSecret: false,
    products: [],
  };

  // Temperature: setTemperature(EnumTemperature.XXX)
  const tempMatch = body.match(/setTemperature\s*\(\s*EnumTemperature\.(\w+)/);
  if (tempMatch) {
    details.temperature = tempMatch[1];
  }

  // Humidity: setHumidity(EnumHumidity.XXX)
  const humidMatch = body.match(/setHumidity\s*\(\s*EnumHumidity\.(\w+)/);
  if (humidMatch) {
    details.humidity = humidMatch[1];
  }

  // Effect: setHasEffect()
  if (body.includes("setHasEffect()")) {
    details.hasEffect = true;
  }

  // Secret: setIsSecret()
  if (body.includes("setIsSecret()")) {
    details.isSecret = true;
  }

  // Products: addProduct(item, chance)
  const productPattern = /addProduct\s*\(\s*([^,]+)\s*,\s*([\\d.]+)f?\s*\)/g;
  let productMatch;
  while ((productMatch = productPattern.exec(body)) !== null) {
    const [, item, chance] = productMatch;
    details.products.push({
      item: parseItemReference(item),
      chance: parseFloat(chance),
    });
  }

  // Specialties: addSpecialty(item, chance)
  const specialtyPattern =
    /addSpecialty\s*\(\s*([^,]+)\s*,\s*([\\d.]+)f?\s*\)/g;
  let specialtyMatch;
  while ((specialtyMatch = specialtyPattern.exec(body)) !== null) {
    const [, item, chance] = specialtyMatch;
    details.products.push({
      item: parseItemReference(item),
      chance: parseFloat(chance),
    });
  }

  return details;
}

/**
 * Parse item reference to a readable format
 */
function parseItemReference(itemRef) {
  itemRef = itemRef.trim();

  // MagicBees combs: Config.combs.get(EnumCombType.TYPE)
  const combMatch = itemRef.match(
    /Config\.combs\.get\s*\(\s*EnumCombType\.(\w+)/
  );
  if (combMatch) {
    return `magicbees:comb.${combMatch[1].toLowerCase()}`;
  }

  // Vanilla items: Items.XXX
  const itemMatch = itemRef.match(/Items\.(\w+)/);
  if (itemMatch) {
    return `minecraft:${itemMatch[1].toLowerCase()}`;
  }

  // Direct item references
  return itemRef;
}
/**
 * Parse mutations from a specific bee's registerMutations() method body
 */
function parseBeeMutations(body, enumName, bees, filePath, bodyStartLine) {
  const mutations = [];

  // Extract registerMutations() method body
  const mutationMethodMatch = body.match(
    /registerMutations\s*\(\s*\)\s*\{([\s\S]*?)\n\s{8}\}/
  );
  if (!mutationMethodMatch) {
    return mutations;
  }

  const mutationBody = mutationMethodMatch[1];
  const mutationBodyStartOffset =
    mutationMethodMatch.index + mutationMethodMatch[0].indexOf("{") + 1;

  // Pattern: registerMutation(PARENT1, PARENT2, CHANCE)...
  // Can be chained with .restrictBiomeType(), .requireResource(), .addMutationCondition(), etc.
  const mutationPattern =
    /registerMutation\s*\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([\d.]+)f?\s*\)((?:\.(?:restrictBiomeType|requireResource|requireNight|requireDay|addMutationCondition)\s*\([^)]*\))*)/g;

  let match;
  while ((match = mutationPattern.exec(mutationBody)) !== null) {
    const [, parent1, parent2, chance, chainedMethods] = match;

    // Calculate line number: bodyStartLine + lines in body before mutationBody + lines in mutationBody before match
    const linesBeforeMethodStart =
      body.substring(0, mutationBodyStartOffset).split("\n").length - 1;
    const linesInMethodBeforeMatch =
      mutationBody.substring(0, match.index).split("\n").length - 1;
    const lineNumber =
      bodyStartLine + linesBeforeMethodStart + linesInMethodBeforeMatch;

    // Create offspring UID directly from enum name (lowercase, no underscores)
    const mutation = {
      parent1: resolveSpeciesReference(parent1.trim(), bees),
      parent2: resolveSpeciesReference(parent2.trim(), bees),
      offspring: `magicbees:${enumName.toLowerCase().replace(/_/g, "")}`,
      chance: parseFloat(chance),
      source: {
        file: filePath,
        line: lineNumber,
      },
    };

    // Parse chained mutation conditions
    if (chainedMethods) {
      const parsedConditions = parseChainedConditions(chainedMethods);
      if (Object.keys(parsedConditions).length > 0) {
        mutation.conditions = parsedConditions;
      }
    }

    mutations.push(mutation);
  }

  return mutations;
}

/**
 * Parse mutations from registerMutations() or similar method
 */
function parseMutations(content, bees) {
  const mutations = [];

  // Extract mutation registration section
  const mutationMatch = content.match(
    /(?:registerMutations|private\s+void\s+\w*[Mm]utation\w*)\s*\([^)]*\)\s*\{([\s\S]*?)(?:\n\s*\}|\n\s*private|\n\s*public)/
  );
  if (!mutationMatch) {
    console.warn("Could not find mutation registration method");
    return mutations;
  }

  const mutationBody = mutationMatch[1];

  // Pattern: registerMutation(PARENT1, PARENT2, OFFSPRING, CHANCE, [conditions]);
  const mutationPattern =
    /registerMutation\s*\(\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+(?:\[\d+\])?)\s*,\s*([\\d.]+)f?\s*(?:,\s*([^)]+))?\)/g;

  let match;
  while ((match = mutationPattern.exec(mutationBody)) !== null) {
    const [, parent1, parent2, offspring, chance, conditions] = match;

    const mutation = {
      parent1: resolveSpeciesReference(parent1, bees),
      parent2: resolveSpeciesReference(parent2, bees),
      offspring: resolveSpeciesReference(
        offspring.replace(/\[\d+\]/, ""),
        bees
      ),
      chance: parseFloat(chance),
    };

    // Parse conditions
    if (conditions) {
      const parsedConditions = parseConditions(conditions);
      if (Object.keys(parsedConditions).length > 0) {
        mutation.conditions = parsedConditions;
      }
    }

    mutations.push(mutation);
  }

  return mutations;
}

/**
 * Create mutations based on the registerMundaneMutations() method
 * This method creates mutations for mundane bees (MYSTICAL, SORCEROUS, UNUSUAL, ATTUNED)
 */
function createMundaneMutations(beeUID, filePath, lineNumber) {
  const mutations = [];

  // Forestry mundane bees that participate in these mutations
  const forestryMundane = [
    "Forest",
    "Meadows",
    "Modest",
    "Wintry",
    "Tropical",
    "Marshy",
  ];

  // Each mundane bee + each Forestry mundane bee → Common (15% chance)
  for (const forestryBee of forestryMundane) {
    mutations.push({
      parent1: beeUID,
      parent2: `forestry:${forestryBee.toLowerCase()}`,
      offspring: "forestry:common",
      chance: 15,
      source: {
        file: filePath,
        line: lineNumber,
        note: "Generated from registerMundaneMutations()",
      },
    });
  }

  // Each mundane bee + Common → Cultivated (12% chance)
  mutations.push({
    parent1: beeUID,
    parent2: "forestry:common",
    offspring: "forestry:cultivated",
    chance: 12,
    source: {
      file: filePath,
      line: lineNumber,
      note: "Generated from registerMundaneMutations()",
    },
  });

  // Each mundane bee + Cultivated → Eldritch (12% chance)
  mutations.push({
    parent1: beeUID,
    parent2: "forestry:cultivated",
    offspring: "magicbees:eldritch",
    chance: 12,
    source: {
      file: filePath,
      line: lineNumber,
      note: "Generated from registerMundaneMutations()",
    },
  });

  return mutations;
}

/**
 * Parse chained mutation conditions from method calls
 */
function parseChainedConditions(chainStr) {
  const conditions = {};

  // restrictBiomeType(BiomeDictionary.Type.XXX) or restrictBiomeType(BiomeDictionary.Type.XXX, BiomeDictionary.Type.YYY)
  const biomeMatches = chainStr.matchAll(/BiomeDictionary\.Type\.(\w+)/g);
  const biomes = [];
  for (const match of biomeMatches) {
    biomes.push(match[1]);
  }
  if (biomes.length > 0) {
    conditions.biome = biomes;
  }

  // requireResource(block) or requireResource("oreDict")
  const resourceMatch = chainStr.match(/requireResource\s*\(\s*([^)]+)\s*\)/);
  if (resourceMatch) {
    const resource = resourceMatch[1].trim();
    if (resource.startsWith('"')) {
      // Ore dictionary string
      conditions.block = [resource.replace(/"/g, "")];
    } else if (resource.includes(".getDefaultState()")) {
      // Block reference like Blocks.WATER.getDefaultState()
      const blockMatch = resource.match(/Blocks\.(\w+)/);
      if (blockMatch) {
        conditions.block = [`minecraft:${blockMatch[1].toLowerCase()}`];
      }
    }
  }

  // requireNight()
  if (chainStr.includes("requireNight()")) {
    conditions.time = "night";
  }

  // requireDay()
  if (chainStr.includes("requireDay()")) {
    conditions.time = "day";
  }

  // addMutationCondition(new MoonPhaseMutationRestriction(MoonPhase.XXX, MoonPhase.YYY))
  const moonRestrictionMatch = chainStr.match(
    /MoonPhaseMutationRestriction\s*\(\s*MoonPhase\.(\w+)(?:\s*,\s*MoonPhase\.(\w+))?\s*\)/
  );
  if (moonRestrictionMatch) {
    const [, phase1, phase2] = moonRestrictionMatch;
    if (phase2 && phase2 !== phase1) {
      conditions.moonPhase = [phase1, phase2];
    } else {
      conditions.moonPhase = [phase1];
    }
  }

  // addMutationCondition(new MoonPhaseMutationBonus(MoonPhase.XXX, MoonPhase.YYY, multiplier))
  const moonBonusMatch = chainStr.match(
    /MoonPhaseMutationBonus\s*\(\s*MoonPhase\.(\w+)\s*,\s*MoonPhase\.(\w+)\s*,\s*([\d.]+)f?\s*\)/
  );
  if (moonBonusMatch) {
    const [, phase1, phase2, multiplier] = moonBonusMatch;
    if (!conditions.moonPhase) {
      conditions.moonPhase = phase1 === phase2 ? [phase1] : [phase1, phase2];
    }
    conditions.moonPhaseBonus = parseFloat(multiplier);
  }

  // addMutationCondition(BeeIntegrationInterface.TCVisMutationRequirement.apply(amount))
  const tcVisMatch = chainStr.match(
    /TCVisMutationRequirement\.apply\s*\(\s*(\d+)\s*\)/
  );
  if (tcVisMatch) {
    conditions.thaumcraftVis = parseInt(tcVisMatch[1]);
  }

  return conditions;
}

/**
 * Resolve species reference (enum name to UID)
 */
function resolveSpeciesReference(ref, bees) {
  ref = ref.trim();

  // Look up in MagicBees species (by enum name) - handles already-parsed bees
  for (const [uid, bee] of Object.entries(bees)) {
    if (bee._enumName === ref) {
      return uid;
    }
  }

  // Pattern: EnumBeeSpecies.getForestrySpecies("Name")
  const forestryMatch = ref.match(/getForestrySpecies\s*\(\s*"(\w+)"/);
  if (forestryMatch) {
    return `forestry:${forestryMatch[1].toLowerCase()}`;
  }

  // Pattern: ExtraBeeDefinition.XXX
  const extraBeesMatch = ref.match(/ExtraBeeDefinition\.(\w+)/);
  if (extraBeesMatch) {
    const name =
      extraBeesMatch[1].charAt(0).toUpperCase() +
      extraBeesMatch[1].slice(1).toLowerCase();
    return `extrabees:${name.toLowerCase()}`;
  }

  // Pattern: Bare ALL_CAPS reference - assume MagicBees if not found in dictionary yet
  // This handles forward references to bees defined later in the file
  if (ref.match(/^[A-Z_]+$/)) {
    // Remove underscores for consistent UID format (e.g., "AE_SKYSTONE" → "aeskystone")
    return `magicbees:${ref.toLowerCase().replace(/_/g, "")}`;
  }

  return ref;
}

/**
 * Parse MagicBees lang file to extract bee names
 * @param {string} langFilePath - Path to en_US.lang file
 * @returns {Object} Map of bee UID to display name
 */
function parseMagicBeesLangFile(langFilePath) {
  const nameMap = {};

  if (!fs.existsSync(langFilePath)) {
    console.warn(`Lang file not found: ${langFilePath}`);
    return nameMap;
  }

  const content = fs.readFileSync(langFilePath, "utf-8");
  const lines = content.split("\n");

  // Pattern: magicbees.species<EnumName>=<Display Name>
  // EnumName matches the Java enum (e.g., AESkystone, TEBlizzy)
  const namePattern = /^magicbees\.species([A-Z]\w+)=(.+)$/;

  for (const line of lines) {
    const match = line.trim().match(namePattern);
    if (match) {
      const [, langEnumName, displayName] = match;
      // Convert langEnumName to normalized UID format for lookup
      // Lang file uses CamelCase: "AESkystone" → "aeskystone"
      // But we need to match UIDs that preserve underscores from Java enums
      // e.g., Java "AE_SKYSTONE" → UID "ae_skystone", Lang "AESkystone" → "aeskystone"
      // Store both with and without underscores to handle matching
      const normalizedName = langEnumName.toLowerCase();
      nameMap[normalizedName] = displayName.trim();
    }
  }

  return nameMap;
}

/**
 * Apply lang names to parsed bees using normalized key matching
 * @param {Object} bees - Parsed bees object
 * @param {Object} nameMap - Map of normalized names to display names
 */
function applyLangNames(bees, nameMap) {
  for (const [uid, bee] of Object.entries(bees)) {
    // Extract the bee name part from UID (e.g., "magicbees:aeskystone" → "aeskystone")
    const beeId = uid.split(":")[1];
    // UIDs are already normalized (lowercase, no underscores)
    if (nameMap[beeId]) {
      bee.name = nameMap[beeId];
    }
  }
}

/**
 * Main export function
 */
function parseMagicBees(javaFilePath, langFilePath = null) {
  const result = parseMagicBeesSpecies(javaFilePath);

  // If lang file path provided, read names from it
  if (langFilePath) {
    const nameMap = parseMagicBeesLangFile(langFilePath);
    // Apply names using normalized key matching
    applyLangNames(result.bees, nameMap);
  }

  return result;
}

module.exports = { parseMagicBees };

// CLI usage
if (require.main === module) {
  const args = process.argv.slice(2);
  if (args.length < 1) {
    console.log(
      "Usage: node magicbees_parser.js <path-to-EnumBeeSpecies.java> [output-json-file]"
    );
    process.exit(1);
  }

  const inputPath = args[0];
  const outputPath = args[1];

  const result = parseMagicBees(inputPath);

  if (outputPath) {
    fs.writeFileSync(outputPath, JSON.stringify(result, null, 2));
    console.log(`Output written to: ${outputPath}`);
  } else {
    console.log(JSON.stringify(result, null, 2));
  }
}
