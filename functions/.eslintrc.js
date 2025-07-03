/** ESLint config for Cloud Functions */
module.exports = {
  root: true,
  env: {es6: true, node: true},
  parser: "@typescript-eslint/parser",
  parserOptions: {
    project: ["tsconfig.json", "tsconfig.dev.json"],
    sourceType: "module",
  },

  // קבצים / תיקיות ש-ESLint יתעלם מהם
  ignorePatterns: [
    "node_modules/**",
    "lib/**",
    "generated/**",
    "index.js",
  ],

  extends: [
    "eslint:recommended",
    "plugin:import/errors",
    "plugin:import/warnings",
    "plugin:import/typescript",
    "google",
    "plugin:@typescript-eslint/recommended",
  ],

  plugins: ["@typescript-eslint", "import"],

  rules: {
    "quotes": ["error", "double"],
    "indent": ["error", 2],
    "import/no-unresolved": 0,
  },
};
