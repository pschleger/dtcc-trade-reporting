# DTCC Reporting Task Decomposition

Decompose the following task for an experienced financial markets business analyst 
specializing in post-trade regulatory reporting, who must produce a complete 
specification package for an OTC derivatives trade position keeping and DTCC regulatory reporting
system that is based on an event driven architecture specified using the attached instructions. 
It should accept FpML trade confirmation messages, build and maintain trade positions from these,
and manage all GTR reporting requirements. It should include feeds for capturing and holding all
relevant static data needed for reporting, such as counterparty static data. Define all business
objects and their lifecycle workflows.

The entire system's business logic must be encapsulated in the entities, their workflow configuration and the functional requirement for each processor and criterion in workflow.

The analyst will need to generate at least the following artefacts:

- The full set of entities using JSON Schema
- For each entity, provide their workflow specification as a separate file using the Cyoda Workflow Schema as defined in @tasks/dtcc-reporting/workflow-config-guide.md. When specifying Criteria, do not use `"type": "simple"`, use `"type": "function"` instead, or `"type": "group"` if needed.
- For each workflow configuration, provide a Markdown document summarizing the workflow and for each processor and criterion, provide a functional specification
- Produce a Markdown document with a list of external interfaces with a technical and functional description for each.
- A Markdown document with the list of all incoming events and swimlane diagrams, including all involved entities. If the list is too large, split it into multiple documents.
- A full description of the system in Markdown with navigation links (once the specification is complete)