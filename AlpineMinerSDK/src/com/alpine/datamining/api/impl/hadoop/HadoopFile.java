/**
 * ClassName HadoopFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.List;

/**
 * @author Eason
 *
 */

public class HadoopFile {

    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_FOLDER = "DIRECTORY";
    public static final String MR_RESULT_FILE_START = "part-";
    private static final String SEPARATOR = "/";
    private String type;
    private String name;
    private int blockSize;
    private String group;
    private int length;
    private String owner;
    private String permission;
    private long modificationTime;
    String parentDir;

    public String getParentDir() {
        return parentDir;
    }

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    // getfulklpath ...
    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HadoopFile> getChildren() {
        return children;
    }

    public void setChildren(List<HadoopFile> children) {
        this.children = children;
    }

    private List<HadoopFile> children;

    public String getFullPath() {
        if (parentDir != null && parentDir.endsWith(SEPARATOR) == false) {
            parentDir = parentDir + SEPARATOR;
        }
        return getParentDir() + getName();
    }

}
