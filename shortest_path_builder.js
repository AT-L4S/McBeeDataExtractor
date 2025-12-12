/**
 * Shortest Path Builder
 *
 * Calculates the shortest breeding path to obtain each bee species,
 * preserving conditional mutations properly.
 */

const fs = require("fs");
const path = require("path");

/**
 * Build shortest breeding path mutations
 * @param {Object} allBees - Map of all bee species
 * @param {Array} allMutations - All mutation data from mutations.jsonc
 * @returns {Array} Shortest path mutations in same format as mutations.jsonc
 */
function buildShortestPathMutations(allBees, allMutations) {
  // Calculate base/wild bees (bees that have no mutations to produce them)
  // These bees must be obtained through other means (found in world, hives, creative, etc.)

  // First, identify all bees that appear as offspring in mutations
  const beesWithMutations = new Set();
  allMutations.forEach((group) => {
    Object.keys(group.children).forEach((offspring) => {
      beesWithMutations.add(offspring);
    });
  });

  // Base bees are those that exist but have no mutations to produce them
  const baseBees = new Set();
  Object.keys(allBees).forEach((beeId) => {
    if (!beesWithMutations.has(beeId)) {
      baseBees.add(beeId);
    }
  });

  console.log(`Found ${baseBees.size} base bees (bees without mutations)`);

  // Track shortest path depth to each bee
  const beeDepth = new Map();
  baseBees.forEach((bee) => beeDepth.set(bee, 0));

  // Build mutation graph: offspring -> [{parents, chance, requirements, depth}]
  const mutationGraph = new Map();

  // Parse all mutations into the graph
  allMutations.forEach((group) => {
    const [parent1, parent2] = group.parents;

    Object.entries(group.children).forEach(([offspring, childData]) => {
      if (!mutationGraph.has(offspring)) {
        mutationGraph.set(offspring, []);
      }

      const mutations = mutationGraph.get(offspring);

      // Handle both unconditional and conditional mutations
      if (childData.requirements && childData.requirements.length > 0) {
        // Each requirement is a separate mutation path
        childData.requirements.forEach((req) => {
          mutations.push({
            parents: [parent1, parent2],
            chance: req.chance !== undefined ? req.chance : childData.chance,
            requirements: req,
            isConditional: true,
          });
        });
      } else {
        // Unconditional mutation
        mutations.push({
          parents: [parent1, parent2],
          chance: childData.chance,
          requirements: null,
          isConditional: false,
        });
      }
    });
  });

  // BFS to calculate shortest paths
  const queue = Array.from(baseBees);

  while (queue.length > 0) {
    const current = queue.shift();
    const currentDepth = beeDepth.get(current);

    // Check all mutations to see what new bees can be created
    mutationGraph.forEach((mutations, offspring) => {
      if (beeDepth.has(offspring)) return; // Already found shortest path to this bee

      // Check if any mutation can produce this offspring
      for (const mutation of mutations) {
        const [p1, p2] = mutation.parents;
        const depth1 = beeDepth.get(p1);
        const depth2 = beeDepth.get(p2);

        if (depth1 !== undefined && depth2 !== undefined) {
          // Both parents are reachable
          const maxParentDepth = Math.max(depth1, depth2);
          const offspringDepth = maxParentDepth + 1;

          if (!beeDepth.has(offspring)) {
            beeDepth.set(offspring, offspringDepth);
            queue.push(offspring);
          }
          break; // Found a path to this offspring, don't need to check other mutations
        }
      }
    });
  }

  // Build shortest path mutations
  // For each bee, find ONLY THE FIRST mutation at the shortest depth
  const shortestMutations = new Map();
  const processedBees = new Set(); // Track which bees we've already added

  mutationGraph.forEach((mutations, offspring) => {
    const targetDepth = beeDepth.get(offspring);
    if (targetDepth === undefined) return; // Unreachable bee
    if (processedBees.has(offspring)) return; // Already found a path for this bee

    // Find the first mutation at the shortest depth
    for (const mutation of mutations) {
      const [p1, p2] = mutation.parents;
      const depth1 = beeDepth.get(p1);
      const depth2 = beeDepth.get(p2);

      if (depth1 === undefined || depth2 === undefined) continue;

      const maxParentDepth = Math.max(depth1, depth2);
      if (maxParentDepth + 1 === targetDepth) {
        // This is a shortest path mutation - add it and mark this bee as processed
        const parentKey = [p1, p2].sort().join("|");

        if (!shortestMutations.has(parentKey)) {
          shortestMutations.set(parentKey, {
            parents: [p1, p2].sort(),
            children: {},
          });
        }

        const group = shortestMutations.get(parentKey);

        group.children[offspring] = {
          chance: mutation.chance,
        };

        // Add requirement if this is a conditional mutation
        if (mutation.isConditional && mutation.requirements) {
          group.children[offspring].requirements = [mutation.requirements];
        }

        processedBees.add(offspring);
        break; // Only take the first shortest path for this bee
      }
    }
  });

  // Convert to output format
  const output = Array.from(shortestMutations.values());

  // Sort groups by parents
  output.sort((a, b) => {
    const parent1Compare = a.parents[0].localeCompare(b.parents[0]);
    if (parent1Compare !== 0) return parent1Compare;
    return a.parents[1].localeCompare(b.parents[1]);
  });

  // Clean up and sort children
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

  return output;
}

/**
 * Format shortest mutations JSON with custom formatting
 */
function formatShortestMutationsJson(data, indent = 0) {
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
        const formattedItem = formatShortestMutationsJson(item, indent + 1);
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
      const formattedValue = formatShortestMutationsJson(value, indent + 1);
      return `${nextIndent}"${key}": ${formattedValue}`;
    });

    return "{\n" + items.join(",\n") + "\n" + indentStr + "}";
  } else {
    return JSON.stringify(data);
  }
}

/**
 * Write shortest mutations JSONC file
 */
function writeShortestMutationsJsonc(filePath, data) {
  const header = `// Shortest Breeding Paths\n// Generated from mutations.jsonc\n// Contains only the shortest breeding path to obtain each bee\n\n`;

  const jsonContent = formatShortestMutationsJson(data);
  fs.writeFileSync(filePath, header + jsonContent);
}

module.exports = {
  buildShortestPathMutations,
  writeShortestMutationsJsonc,
};
