package com.vertyll.fastprod.translation;

import java.util.Set;
import java.util.TreeSet;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.MessagePattern;

final class IcuMessages {

    private IcuMessages() {
    }

    static boolean isValid(String message) {
        try {
            new MessageFormat(message);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }

    static Set<String> placeholders(String message) {
        MessagePattern pattern = new MessagePattern(message);
        Set<String> names = new TreeSet<>();
        for (int i = 0; i < pattern.countParts(); i++) {
            MessagePattern.Part part = pattern.getPart(i);
            if (part.getType() == MessagePattern.Part.Type.ARG_NAME) {
                names.add(pattern.getSubstring(part));
            }
        }
        return names;
    }
}
