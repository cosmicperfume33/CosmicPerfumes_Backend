package com.example.ForgeX.model;

import java.util.Map;

/** GST state codes, used to decide CGST+SGST versus IGST. */
public final class StateCode {

    private static final Map<String, String> CODES = Map.ofEntries(
            Map.entry("JAMMU AND KASHMIR", "01"), Map.entry("HIMACHAL PRADESH", "02"),
            Map.entry("PUNJAB", "03"), Map.entry("CHANDIGARH", "04"),
            Map.entry("UTTARAKHAND", "05"), Map.entry("HARYANA", "06"),
            Map.entry("DELHI", "07"), Map.entry("RAJASTHAN", "08"),
            Map.entry("UTTAR PRADESH", "09"), Map.entry("BIHAR", "10"),
            Map.entry("SIKKIM", "11"), Map.entry("ARUNACHAL PRADESH", "12"),
            Map.entry("NAGALAND", "13"), Map.entry("MANIPUR", "14"),
            Map.entry("MIZORAM", "15"), Map.entry("TRIPURA", "16"),
            Map.entry("MEGHALAYA", "17"), Map.entry("ASSAM", "18"),
            Map.entry("WEST BENGAL", "19"), Map.entry("JHARKHAND", "20"),
            Map.entry("ODISHA", "21"), Map.entry("CHHATTISGARH", "22"),
            Map.entry("MADHYA PRADESH", "23"), Map.entry("GUJARAT", "24"),
            Map.entry("MAHARASHTRA", "27"), Map.entry("KARNATAKA", "29"),
            Map.entry("GOA", "30"), Map.entry("LAKSHADWEEP", "31"),
            Map.entry("KERALA", "32"), Map.entry("TAMIL NADU", "33"),
            Map.entry("PUDUCHERRY", "34"), Map.entry("ANDAMAN AND NICOBAR ISLANDS", "35"),
            Map.entry("TELANGANA", "36"), Map.entry("ANDHRA PRADESH", "37"),
            Map.entry("LADAKH", "38"));

    private StateCode() { }

    public static String of(String stateName) {
        if (stateName == null) return "";
        return CODES.getOrDefault(stateName.trim().toUpperCase(), "");
    }

    public static boolean sameState(String a, String b) {
        String ca = of(a);
        String cb = of(b);
        return !ca.isEmpty() && ca.equals(cb);
    }
}