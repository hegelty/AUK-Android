package com.unrevr.munhaeryeok.Alarm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Version 22 stored the visible groups and their platform request codes in separate files. */
final class LegacyAlarms {
    private static final Pattern GROUP = Pattern.compile(
            "(?:^|=)([0-9]+)\\|([0-9]+):([0-9]+)\\|(true|false)\\|(true|false)\\|(.*?)"
                    + "\\|([123])\\|(true|false)\\|((?:[0-9]+,){6}[0-9]+,?)(?==|$)", Pattern.DOTALL);
    private static final Pattern ENTRY = Pattern.compile(
            "(?:^|=)([0-9]+)-([0-9]+)-([1-7]):([0-9]+):([0-9]+):[0-9]+-(.*?)"
                    + "\\|([01])\\|([01])\\|([123])(?==|$)", Pattern.DOTALL);

    final ArrayList<AlarmData> groups = new ArrayList<>();
    final Set<Integer> requestCodes = new HashSet<>();

    LegacyAlarms(String groupFile, String entryFile) {
        Set<Integer> groupedEntries = new HashSet<>();
        Set<Integer> groupIds = new HashSet<>();
        Matcher group = GROUP.matcher(groupFile.trim());
        while (group.find()) {
            try {
                int id = number(group, 1);
                if (id <= 0 || groupIds.contains(id)) continue;
                int days = 0;
                ArrayList<Integer> members = new ArrayList<>();
                String[] ids = group.group(9).split(",");
                for (int i = 0; i < 7; i++) {
                    int member = Integer.parseInt(ids[i]);
                    if (member != 0) {
                        days |= 1 << i;
                        members.add(member);
                    }
                }
                groups.add(new AlarmData(id, number(group, 2), number(group, 3), days,
                        Boolean.parseBoolean(group.group(4)), Boolean.parseBoolean(group.group(5)),
                        group.group(6), number(group, 7), Boolean.parseBoolean(group.group(8))));
                groupedEntries.addAll(members);
                groupIds.add(id);
            } catch (IllegalArgumentException invalidRow) {
                // A partial row cannot prevent recovery of the remaining groups and individual days.
            }
        }
        Matcher entry = ENTRY.matcher(entryFile.trim());
        while (entry.find()) {
            try {
                int code = number(entry, 2);
                if (code != 0) requestCodes.add(code);
                int member = number(entry, 1);
                if (!groupedEntries.contains(member)) {
                    groups.add(new AlarmData(0, number(entry, 4), number(entry, 5), 1 << (number(entry, 3) - 1),
                            number(entry, 7) == 1, number(entry, 8) == 1, entry.group(6), number(entry, 9), false));
                    groupedEntries.add(member);
                }
            } catch (IllegalArgumentException invalidRow) {
                // Retain the original files for recovery.
            }
        }
    }

    private static int number(Matcher record, int field) {
        return Integer.parseInt(record.group(field));
    }
}
