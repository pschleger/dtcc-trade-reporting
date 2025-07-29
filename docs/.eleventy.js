module.exports = function(eleventyConfig) {
  // Copy static assets
  eleventyConfig.addPassthroughCopy("css");
  eleventyConfig.addPassthroughCopy("images");
  eleventyConfig.addPassthroughCopy("assets");

  // Copy JSON schema files as static assets
  eleventyConfig.addPassthroughCopy("content/specification/schema/**/*.json");
  
  // Set up markdown processing
  eleventyConfig.setLibrary("md", require("markdown-it")({
    html: true,
    breaks: false,
    linkify: true
  }));
  
  // Add a filter to create navigation-friendly titles
  eleventyConfig.addFilter("navTitle", function(str) {
    return str
      .replace(/\.md$/, '')
      .replace(/-/g, ' ')
      .replace(/\b\w/g, l => l.toUpperCase());
  });
  
  // Add a filter to get directory name
  eleventyConfig.addFilter("dirname", function(str) {
    const path = require('path');
    return path.dirname(str);
  });
  
  // Add a filter to get relative path from root
  eleventyConfig.addFilter("relativePath", function(str) {
    return str.replace(/^docs\//, '');
  });
  
  // Add collection for all documentation pages
  eleventyConfig.addCollection("docs", function(collectionApi) {
    return collectionApi.getFilteredByGlob("content/**/*.md");
  });

  // Add collection for specification pages
  eleventyConfig.addCollection("specification", function(collectionApi) {
    return collectionApi.getFilteredByGlob("content/specification/**/*.md");
  });

  // Add a filter to get top-level content folders
  eleventyConfig.addFilter("getTopLevelFolders", function(collection) {
    const folders = new Set();
    collection.forEach(item => {
      const pathParts = item.inputPath.replace('./content/', '').split('/');
      if (pathParts.length > 1) {
        folders.add(pathParts[0]);
      }
    });
    return Array.from(folders).sort();
  });

  // Add a function to build navigation tree
  eleventyConfig.addFilter("buildNavTree", function(collection) {
    const tree = {};

    collection.forEach(item => {
      const pathParts = item.inputPath.replace('./content/', '').replace('.md', '').split('/');
      let current = tree;

      pathParts.forEach((part, index) => {
        if (!current[part]) {
          current[part] = {
            name: part,
            path: pathParts.slice(0, index + 1).join('/'),
            children: {},
            items: [],
            isFolder: index < pathParts.length - 1
          };
        }

        if (index === pathParts.length - 1) {
          // This is a file
          current[part].item = item;
          current[part].isFolder = false;
        } else {
          // This is a folder, move to next level
          current = current[part].children;
        }
      });
    });

    return tree;
  });
  
  return {
    dir: {
      input: ".",
      output: "_site",
      includes: "_includes",
      data: "_data"
    },
    markdownTemplateEngine: "njk",
    htmlTemplateEngine: "njk",
    dataTemplateEngine: "njk"
  };
};
