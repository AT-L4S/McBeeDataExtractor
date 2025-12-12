/**
 * Find all bees that have no mutations to produce them
 * These should be added to the base bees list
 */

const fs = require("fs");
const path = require("path");

// Read the mutations file
const mutationsPath = path.join(__dirname, "data", "mutations.jsonc");
const mutationsContent = fs.readFileSync(mutationsPath, "utf-8");

// Remove comments and parse JSON
const mutationsJson = mutationsContent
  .split("\n")
  .filter((line) => !line.trim().startsWith("//"))
  .join("\n");
const allMutations = JSON.parse(mutationsJson);

// Read the bees file
const beesPath = path.join(__dirname, "data", "bees.jsonc");
const beesContent = fs.readFileSync(beesPath, "utf-8");
const beesJson = beesContent
  .split("\n")
  .filter((line) => !line.trim().startsWith("//"))
  .join("\n");
const allBees = JSON.parse(beesJson);

// Get all bee IDs
const allBeeIds = new Set(Object.keys(allBees));

// Get all bees that appear as offspring in mutations
const beesWithMutations = new Set();
allMutations.forEach((group) => {
  Object.keys(group.children).forEach((offspring) => {
    beesWithMutations.add(offspring);
  });
});

// Find bees without mutations
const beesWithoutMutations = [];
allBeeIds.forEach((beeId) => {
  if (!beesWithMutations.has(beeId)) {
    beesWithoutMutations.push(beeId);
  }
});

// Sort by mod
beesWithoutMutations.sort();

console.log(`\nFound ${beesWithoutMutations.length} bees without mutations:\n`);

// Group by mod
const byMod = {};
beesWithoutMutations.forEach((beeId) => {
  const mod = beeId.split(":")[0];
  if (!byMod[mod]) byMod[mod] = [];
  byMod[mod].push(beeId);
});

Object.keys(byMod)
  .sort()
  .forEach((mod) => {
    console.log(`\n${mod} (${byMod[mod].length} bees):`);
    byMod[mod].forEach((beeId) => {
      console.log(`  "${beeId}",`);
    });
  });

console.log(`\nTotal: ${beesWithoutMutations.length} unreachable bees`);
console.log(
  `Total bees with mutations: ${beesWithMutations.size} / ${allBeeIds.size}`
);
