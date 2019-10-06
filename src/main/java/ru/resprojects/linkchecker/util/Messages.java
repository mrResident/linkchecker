package ru.resprojects.linkchecker.util;

import static ru.resprojects.linkchecker.util.ValidationUtil.*;

public final class Messages {

    public static final String MSG_ARGUMENT_NULL = "Argument must not be null";
    public static final String MSG_COLLECTION_EMPTY = "Collection must not be empty";
    public static final String MSG_COLLECTION_CONTAIN_NULL = "Collection must not contain a null item";

    public static final String MSG_BY_ID_ERROR = "%s with ID = %d is not found";

    public static final String NODE_MSG_UPDATE_ERROR = "Error while update node with id = ";
    public static final String NODE_MSG_BY_NAME_ERROR = "Node with NAME = %s is not found";
    public static final String NODE_MSG_BY_OBJECT_ERROR = "Node %s is not found";
    public static final String NODE_MSG_NOT_REACHABLE = "Nodes %s and %s are not reachable to each other";

    public static final String EDGE_MSG_GET_ERROR = "Edge for nodes [%s, %s] is not found";
    public static final String EDGE_MSG_ALREADY_PRESENT_ERROR = "Edge for nodes ([%s, %s], [%s, %s]) already present in the graph";
    public static final String EDGE_MSG_GET_BY_NAME_ERROR = "Edges for node %s is not found";

    public static final String VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE = "Value must not be empty";
    public static final String VALIDATOR_NODE_NAME_RANGE_MESSAGE = "Value must be at range from "
        + MIN_NAME_SIZE + " to " + MAX_NAME_SIZE;
    public static final String VALIDATOR_NOT_NULL_MESSAGE = "Object must not be null";


    private Messages() {
    }

}
