package com.aircraftcarrier.framework.web.client;

/**
 * @author lzp
 * @since 2019-10-14 17:14
 */
public class Pair {
    /**
     * name
     */
    private String name = "";

    /**
     * value
     */
    private String value = "";

    public Pair(String name, String value) {
        setName(name);
        setValue(value);
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        if (!isValidString(name)) {
            return;
        }

        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    private void setValue(String value) {
        if (!isValidString(value)) {
            return;
        }

        this.value = value;
    }

    private boolean isValidString(String arg) {
        if (arg == null) {
            return false;
        }

        return !arg.trim().isEmpty();
    }
}
