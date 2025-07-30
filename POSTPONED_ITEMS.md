# Postponed/Descoped Items

This file tracks items that have been postponed or descoped from the current implementation phase.

## External Interface Integrations - Phase 1 Descoped Items

### Kafka Integration (Action Item 4)
**Status**: Postponed for simplicity
**Original Requirement**: Configure Apache Kafka message consumers for real-time trade confirmation processing
**Details**:
- Apache Kafka message consumers for trade confirmations
- Topic: `trade-confirmations`
- Partition strategy by counterparty LEI
- Message retention: 7 days
- Compression: gzip
- Dead letter queue configuration
- Consumer group management
- Offset management and replay capabilities

**Dependencies**:
- Kafka client libraries
- Kafka configuration in application.yml
- Consumer implementation with error handling
- Integration with existing TradeConfirmationService

**Reason for Postponement**: 
- Simplifying initial implementation to focus on REST API endpoints
- Kafka adds complexity that can be addressed in a later phase
- REST endpoints provide sufficient functionality for initial testing and validation

**Future Implementation Notes**:
- Will need to add Kafka dependencies to build.gradle
- Consumer should integrate with the same TradeConfirmationService used by REST endpoints
- Consider using Spring Kafka for easier integration
- Ensure message ordering and exactly-once processing semantics

## Future Considerations

### Additional Items for Future Phases
- Advanced rate limiting strategies (beyond basic implementation)
- Complex authentication providers integration
- Advanced monitoring and alerting integrations
- Performance optimization and caching strategies
- Advanced error recovery mechanisms
- Batch processing optimizations
