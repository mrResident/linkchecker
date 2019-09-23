package ru.resprojects.linkchecker.util;

import static ru.resprojects.linkchecker.util.ValidationUtil.*;

public final class Messages {

    public static final String MSG_NOT_NULL = "Must not be null";
    public static final String MSG_COLLECTION_EMPTY = "Collection does not be empty";
    public static final String MSG_COLLECTION_CONTAIN_NULL = "Collection does not be contain null element";

    public static final String MSG_BY_ID_ERROR = "%s with ID = %d is not found";

    public static final String NODE_MSG_UPDATE_ERROR = "Error while update node with id = ";
    public static final String NODE_MSG_BY_NAME_ERROR = "Node with NAME = %s is not found";
    public static final String NODE_MSG_BY_OBJECT_ERROR = "Node %s is not found";

    public static final String EDGE_MSG_GET_ERROR = "Edge for nodes [%s, %s] is not found";
    public static final String EDGE_MSG_GET_BY_NAME_ERROR = "Edges for node %s is not found";

    public static final String VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE = "value does not be empty";
    public static final String VALIDATOR_NODE_NAME_RANGE_MESSAGE = "value must be at range from "
        + MIN_NAME_SIZE + " to " + MAX_NAME_SIZE;
    public static final String VALIDATOR_NODE_PROBABILITY_RANGE_MESSAGE = "value must be at range from "
        + MIN_PROBABILITY_VALUE + " to " + MAX_PROBABILITY_VALUE;
    public static final String VALIDATOR_NOT_NULL_MESSAGE = "object does not be null";


    private Messages() {
    }

}
