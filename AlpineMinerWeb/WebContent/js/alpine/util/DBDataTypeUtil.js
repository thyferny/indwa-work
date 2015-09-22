/**
 * User: robbie
 * Date: 12/4/12
 * 2012
 */

define([],function(){

    var _dbTypes = {
        VARCHAR:          "VARCHAR",
        INTEGER:          "INTEGER",
        BIGINT:           "BIGINT",
        NUMERIC:          "NUMERIC",
        BOOLEAN:          "BOOLEAN",
        BIT:              "BIT",
        BIT_VARYING:      "BIT VARYING",
        CHAR:             "CHAR",
        DATE:             "DATE",
        DOUBLE_PRECISION: "DOUBLE PRECISION"
    };

    return {
        dbTypes: _dbTypes
    }

});