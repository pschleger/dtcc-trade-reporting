# How We Got Here

Cyoda is a platform for building event-driven, entity-centric applications. It is based on the principle that every piece of persisted data is an entity, and that entities function as finite state machines governed by workflows. 

The premise is that when business logic is declarative, visual, and deterministic, and the system architecture is coherent and unified, developers are more effective at building and maintaining complex systems. But just as important, the idea is that AIs can be much more effective in such a structured environment.

This project is an experiment to see how well this works, and in particular, to learn and optimize the approach to AI assisted implementation of complex business systems.

> **This project does not have the aim to produce a system that actually satisfies the business requirements for DTCC regulatory reporting**. 

I'm letting the AI take a free hand on this, based on the information embedded in its model concerning this subject matter. I am confident that it can produce requirements that are close enough to at least mimic to a large degree the complexity of this topic, if not being formally correct.

I the real world, a team of business analysts (SMEs) would work together with stakeholders and developers to formulate and refine the requirements. 

I am much more interested in seeing how far an AI can implement the backend system itself with minmal human supervision.

Take it as a prime directive for the project: 

Plan the work properly, state things clearly, watch what the AI does closely, but 
> **Prime Directive**: **Only intervene when necessary**.

To prepare the work, I took the following prompt to get the AI to decompose the high level objective of creating a DTCC reporting system into a set of clearly defined tasks for a business analyst to execute. The BA is again the AI. And so on... 

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