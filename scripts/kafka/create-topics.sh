#!/bin/bash

# A2A Kafka Agent System - Topic Creation Script
# This script creates all required Kafka topics for the A2A system

set -e

# Default configuration
KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS:-"localhost:9092"}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-1}
ENVIRONMENT=${ENVIRONMENT:-"dev"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Kafka is available
check_kafka_availability() {
    print_info "Checking Kafka availability at $KAFKA_BOOTSTRAP_SERVERS..."
    
    if ! kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" --list >/dev/null 2>&1; then
        print_error "Cannot connect to Kafka at $KAFKA_BOOTSTRAP_SERVERS"
        print_error "Please ensure Kafka is running and accessible"
        exit 1
    fi
    
    print_success "Kafka is available"
}

# Function to create a topic if it doesn't exist
create_topic() {
    local topic_name=$1
    local partitions=$2
    local retention_ms=$3
    local cleanup_policy=$4
    local additional_configs=$5
    
    print_info "Creating topic: $topic_name"
    
    # Check if topic already exists
    if kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" --list | grep -q "^${topic_name}$"; then
        print_warning "Topic $topic_name already exists, skipping creation"
        return 0
    fi
    
    # Build the create command
    local create_cmd="kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic $topic_name --partitions $partitions --replication-factor $REPLICATION_FACTOR"
    
    # Add configurations
    local configs="cleanup.policy=$cleanup_policy,compression.type=snappy"
    
    if [ -n "$retention_ms" ] && [ "$cleanup_policy" = "delete" ]; then
        configs="$configs,retention.ms=$retention_ms"
    fi
    
    if [ -n "$additional_configs" ]; then
        configs="$configs,$additional_configs"
    fi
    
    create_cmd="$create_cmd --config $configs"
    
    # Execute the command
    if eval "$create_cmd"; then
        print_success "Created topic: $topic_name"
    else
        print_error "Failed to create topic: $topic_name"
        return 1
    fi
}

# Function to create all A2A topics
create_a2a_topics() {
    print_info "Creating A2A Kafka topics for environment: $ENVIRONMENT"
    print_info "Bootstrap servers: $KAFKA_BOOTSTRAP_SERVERS"
    print_info "Replication factor: $REPLICATION_FACTOR"
    echo
    
    # A2A Tasks Topic
    create_topic "a2a.tasks" 3 "604800000" "delete" "max.message.bytes=1048576"
    
    # A2A Replies Topic
    create_topic "a2a.replies" 3 "604800000" "delete" "max.message.bytes=1048576"
    
    # A2A Events Topic
    create_topic "a2a.events" 6 "86400000" "delete" "max.message.bytes=524288"
    
    # A2A Registry Topic (compacted)
    create_topic "a2a.registry" 1 "" "compact" "segment.ms=86400000,min.cleanable.dirty.ratio=0.1,delete.retention.ms=86400000"
    
    # A2A Dead Letter Queue
    create_topic "a2a.dlq" 1 "2592000000" "delete" ""
    
    echo
    print_success "All A2A topics created successfully!"
}

# Function to list created topics
list_topics() {
    print_info "Listing A2A topics:"
    echo
    
    kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" --list | grep "^a2a\." | while read -r topic; do
        echo -e "${GREEN}✓${NC} $topic"
        
        # Get topic details
        kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" --describe --topic "$topic" | grep -E "(Topic:|Config:)" | sed 's/^/  /'
        echo
    done
}

# Function to delete all A2A topics (for cleanup)
delete_topics() {
    print_warning "This will delete ALL A2A topics. This action cannot be undone!"
    read -p "Are you sure you want to continue? (yes/no): " -r
    
    if [[ ! $REPLY =~ ^yes$ ]]; then
        print_info "Operation cancelled"
        return 0
    fi
    
    print_info "Deleting A2A topics..."
    
    local topics=("a2a.tasks" "a2a.replies" "a2a.events" "a2a.registry" "a2a.dlq")
    
    for topic in "${topics[@]}"; do
        if kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" --list | grep -q "^${topic}$"; then
            if kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" --delete --topic "$topic"; then
                print_success "Deleted topic: $topic"
            else
                print_error "Failed to delete topic: $topic"
            fi
        else
            print_warning "Topic $topic does not exist"
        fi
    done
}

# Function to show help
show_help() {
    echo "A2A Kafka Agent System - Topic Management Script"
    echo
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo
    echo "Commands:"
    echo "  create    Create all A2A topics (default)"
    echo "  list      List all A2A topics with details"
    echo "  delete    Delete all A2A topics (use with caution)"
    echo "  help      Show this help message"
    echo
    echo "Environment Variables:"
    echo "  KAFKA_BOOTSTRAP_SERVERS  Kafka bootstrap servers (default: localhost:9092)"
    echo "  REPLICATION_FACTOR       Topic replication factor (default: 1)"
    echo "  ENVIRONMENT              Environment name (default: dev)"
    echo
    echo "Examples:"
    echo "  $0 create"
    echo "  KAFKA_BOOTSTRAP_SERVERS=kafka:29092 $0 create"
    echo "  REPLICATION_FACTOR=3 $0 create"
    echo "  $0 list"
    echo "  $0 delete"
}

# Main script logic
main() {
    local command=${1:-"create"}
    
    case $command in
        "create")
            check_kafka_availability
            create_a2a_topics
            ;;
        "list")
            check_kafka_availability
            list_topics
            ;;
        "delete")
            check_kafka_availability
            delete_topics
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Unknown command: $command"
            echo
            show_help
            exit 1
            ;;
    esac
}

# Run the main function with all arguments
main "$@"