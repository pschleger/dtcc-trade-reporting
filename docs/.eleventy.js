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

  // Add a split filter
  eleventyConfig.addFilter("split", function(str, delimiter) {
    return str.split(delimiter);
  });

  // Add a first filter to get the first element of an array
  eleventyConfig.addFilter("first", function(array) {
    return Array.isArray(array) ? array[0] : array;
  });

  // Add a filter to check if a folder contains the current page (recursively)
  eleventyConfig.addFilter("containsCurrentPage", function(node, currentUrl) {
    if (!currentUrl || !node) return false;

    // Helper function to check if a node or its children contain the current page
    function nodeContainsCurrentPage(node, currentUrl) {
      // If this is a file node, check if it matches the current URL
      if (node.item && node.item.url === currentUrl) {
        return true;
      }

      // If this is a folder, check all children recursively
      if (node.children) {
        for (const childKey in node.children) {
          if (nodeContainsCurrentPage(node.children[childKey], currentUrl)) {
            return true;
          }
        }
      }

      return false;
    }

    return nodeContainsCurrentPage(node, currentUrl);
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

  // Add a filter to get navigation configuration with fallback to automatic detection
  eleventyConfig.addFilter("getNavigationConfig", function(collection, navigationData) {
    // If navigation configuration exists, use it
    if (navigationData && navigationData.topMenu && navigationData.topMenu.length > 0) {
      // Validate that configured folders actually exist in content
      const existingFolders = new Set();
      collection.forEach(item => {
        const pathParts = item.inputPath.replace('./content/', '').split('/');
        if (pathParts.length > 1) {
          existingFolders.add(pathParts[0]);
        }
      });

      // Filter out configured items that don't have corresponding content folders
      return navigationData.topMenu.filter(item => existingFolders.has(item.id));
    }

    // Fallback to automatic detection
    const folders = new Set();
    collection.forEach(item => {
      const pathParts = item.inputPath.replace('./content/', '').split('/');
      if (pathParts.length > 1) {
        folders.add(pathParts[0]);
      }
    });

    // Convert to navigation format with default icons
    return Array.from(folders).sort().map(folder => ({
      id: folder,
      title: folder.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase()),
      icon: getDefaultIcon(folder),
      description: `${folder.replace('-', ' ')} documentation`
    }));
  });

  // Helper function to get default icons for folders
  function getDefaultIcon(folder) {
    const iconMap = {
      'specification': 'fas fa-file-text',
      'schema': 'fas fa-code',
      'interfaces': 'fas fa-plug',
      'api': 'fas fa-cogs',
      'guides': 'fas fa-book',
      'background': 'fas fa-info-circle',
      'system-specification': 'fas fa-file-text'
    };
    return iconMap[folder.toLowerCase()] || 'fas fa-folder';
  }

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
