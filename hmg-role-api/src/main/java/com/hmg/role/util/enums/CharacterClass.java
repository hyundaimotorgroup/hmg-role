package com.hmg.role.util.enums;

import com.hmg.role.util.validation.annotations.ValidCharacters;
import java.util.regex.Pattern;

/**
 * Character classes possible for validations. Contains regex patterns accepted by {@link
 * Pattern#compile(String)} to be used in {@link ValidCharacters}.
 */
public enum CharacterClass {
    /** Latin alphabets with no diacritics */
    LATIN_ALPHABET("A-Za-z"),
    /** Latin alphabets with diacritics */
    EXTENDED_LATIN_ALPHABET("\\p{L}"), // might be required for latin characters with diacritics
    /** Western Arabic numerals (0-9) */
    ARABIC_NUMERAL("0-9"),
    /** Literal dashes */
    DASH("\\-"),
    /** Literal underscores */
    UNDERSCORE("\\_");

    public final String patternString;

    CharacterClass(String patternString) {
        this.patternString = patternString;
    }
}
