# Navigation Configuration

This documentation site supports explicit control over the top navigation menu through a configuration file.

## Configuration File

The navigation is controlled by `docs/_data/navigation.json`. This file defines the top-level menu items, their order, icons, and descriptions.

### Structure

```json
{
  "topMenu": [
    {
      "id": "folder-name",
      "title": "Display Name",
      "icon": "fas fa-icon-name",
      "description": "Tooltip description"
    }
  ]
}
```

### Properties

- **id**: Must match the exact folder name under `docs/content/`
- **title**: The display name shown in the navigation menu
- **icon**: FontAwesome icon class (e.g., "fas fa-cogs", "fas fa-book")
- **description**: Tooltip text shown on hover

## Example Configuration

```json
{
  "topMenu": [
    {
      "id": "Background",
      "title": "Background",
      "icon": "fas fa-info-circle",
      "description": "Background information and context"
    },
    {
      "id": "System-Specification",
      "title": "System Specification", 
      "icon": "fas fa-cogs",
      "description": "Complete system specification and documentation"
    }
  ]
}
```

## Available Icons

Common FontAwesome icons you can use:

- `fas fa-info-circle` - Information/background
- `fas fa-cogs` - System/technical documentation
- `fas fa-file-text` - Specifications/documents
- `fas fa-book` - Guides/tutorials
- `fas fa-code` - Code/schemas
- `fas fa-plug` - Interfaces/APIs
- `fas fa-chart-line` - Analytics/reports
- `fas fa-shield-alt` - Security/compliance
- `fas fa-database` - Data/entities
- `fas fa-sitemap` - Workflows/processes

## Fallback Behavior

If no navigation configuration is provided, the system automatically:

1. Detects all folders under `docs/content/`
2. Creates menu items in alphabetical order
3. Uses default icons based on folder names
4. Generates titles by capitalizing folder names

## Adding New Sections

To add a new top-level section:

1. Create a folder under `docs/content/`
2. Add the folder to `_data/navigation.json`
3. The system will automatically validate that the folder exists

## Order Control

Menu items appear in the exact order specified in the `topMenu` array. This allows you to control the logical flow of your documentation.
