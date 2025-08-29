/*
 * Created on 24-10-2004
 *
 */
package net.sf.recipetools.javarecipes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ft
 *
 */
// @Entity
public class Unit extends NamedEntity {

    /** Hibernate ID: primary key */
    // @Id
    // @GeneratedValue
    Long id;

    /** the hibernate HBM version */
    // @Version
    int hbmVersion;

    // @Basic
    // @org.hibernate.annotations.Index(name = "IDX_UNIT_NAME")
    private String name;

    /**
	 * 
	 */
    public Unit() {
        super();
    }

    public Unit(int id, String name) {
        super(id, name);
        if (name != null)
            name = name.trim();
    }

    public Unit(String name) {
        super(name);
        if (name != null)
            name = name.trim();
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the hbmVersion
     */
    @Override
    public int getHbmVersion() {
        return hbmVersion;
    }

    /**
     * @param hbmVersion
     *            the hbmVersion to set
     */
    @Override
    public void setHbmVersion(int hbmVersion) {
        this.hbmVersion = hbmVersion;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    // @Transient
    private static HashMap<String, String> aliases = new HashMap<String, String>();
    static {
        aliases.put("gram", "g");
        aliases.put("gm", "g");
        aliases.put("gr", "g");
        aliases.put("grams", "g");
        aliases.put("grammes", "g");
        aliases.put("kilo", "kg");
        aliases.put("kilogram", "kg");
        aliases.put("kilograms", "kg");
        aliases.put("milligram", "mg");
        aliases.put("milligrams", "mg");
        aliases.put("mililiter", "ml");
        aliases.put("milliliter", "ml");
        aliases.put("milliliters", "ml");
        aliases.put("millilitre", "ml");
        aliases.put("millilitres", "ml");
        aliases.put("centiliter", "cl");
        aliases.put("centiliters", "cl");
        aliases.put("centilitre", "cl");
        aliases.put("centilitres", "cl");
        aliases.put("deciliter", "dl");
        aliases.put("deciliters", "dl");
        aliases.put("decilitre", "dl");
        aliases.put("decilitres", "dl");
        aliases.put("metre", "m");
        aliases.put("meter", "m");
        aliases.put("centimeter", "cm");
        aliases.put("centimetre", "cm");
        aliases.put("centimeter", "cm");
        aliases.put("centimetre", "cm");
        aliases.put("milimeter", "mm");
        aliases.put("milimetre", "mm");
        aliases.put("millimeter", "mm");
        aliases.put("millimetre", "mm");
        aliases.put("liter", "l");
        aliases.put("liters", "l");
        aliases.put("litre", "l");
        aliases.put("litres", "l");
        aliases.put("ltr", "l");
        aliases.put("lt", "l");

        // -------------- English ------------------
        aliases.put("bg", "bag");
        aliases.put("bags", "bag");
        aliases.put("balls", "ball");
        aliases.put("bars", "bar");
        aliases.put("barspoons", "barspoon");
        aliases.put("bskt", "basket");
        aliases.put("bskts", "basket");
        aliases.put("baskets", "basket");
        aliases.put("batches", "batch");
        aliases.put("blocks", "block");
        aliases.put("bwl", "bowl");
        aliases.put("bwls", "bowl");
        aliases.put("bowls", "bowl");
        aliases.put("bx", "box");
        aliases.put("bxs", "box");
        aliases.put("boxes", "box");
        aliases.put("bunches", "bunch");
        aliases.put("bn", "bunch");
        aliases.put("bot", "bottle");
        aliases.put("bottles", "bottle");
        aliases.put("bowls", "bowl");
        aliases.put("bu", "bushel");
        aliases.put("bushels", "bushel");
        aliases.put("c", "cup");
        aliases.put("cans", "can");
        aliases.put("cn", "can");
        aliases.put("cartons", "carton");
        aliases.put("ctn", "carton");
        aliases.put("crocks", "crock");
        aliases.put("couple", "couple");
        aliases.put("cp", "cup");
        aliases.put("cups", "cup");
        aliases.put("cupfuls", "cupful");
        aliases.put("cupsful", "cupful");
        aliases.put("cl", "clove");
        aliases.put("clv", "clove");
        aliases.put("cloves", "clove");
        aliases.put("ctnr", "container");
        aliases.put("ctnrs", "container");
        aliases.put("containers", "container");
        aliases.put("ct", "container");
        aliases.put("cnt", "container");
        aliases.put("cubes", "cube");
        aliases.put("doz", "dozen");
        aliases.put("drops", "drop");
        aliases.put("dr", "drop");
        aliases.put("ds", "dash");
        aliases.put("dsh", "dash");
        aliases.put("dashes", "dash"); // = 1/8 tsp
        aliases.put("dessertspoons", "dessertspoon");
        aliases.put("dsp", "dessertspoon");
        aliases.put("dollops", "dollop");
        aliases.put("dz", "dozen");
        aliases.put("ea", "each");
        aliases.put("ears", "ear"); // for corn
        aliases.put("env", "envelope");
        aliases.put("envelopes", "envelope");
        aliases.put("x", "each");
        aliases.put("few", "few");
        aliases.put("fifths", "fifth");
        aliases.put("fillets", "fillet");
        aliases.put("fistfuls", "fistful");
        aliases.put("fingers", "finger"); // ~ 1 ounce
        aliases.put("fluid ounces", "fluid ounce");
        aliases.put("fl oz", "fluid ounce");
        aliases.put("flats", "flat");
        aliases.put("fl", "flat");
        aliases.put("feet", "foot");
        aliases.put("ga", "gallon");
        aliases.put("gal", "gallon");
        aliases.put("gall", "gallon");
        aliases.put("gallons", "gallon");
        aliases.put("gills", "gill"); // = 1/2 cup
        aliases.put("grains", "grain"); // < 1/8 teaspoon
        aliases.put("grinds", "grind");
        aliases.put("handf", "handful");
        aliases.put("handfuls", "handful");
        aliases.put("hd", "head");
        aliases.put("hds", "head");
        aliases.put("heads", "head");
        aliases.put("inch", "inch");
        aliases.put("inches", "inch");
        aliases.put("jars", "jar");
        aliases.put("jiggers", "jigger");
        aliases.put("knobs", "knob");
        aliases.put("layer", "layers");
        aliases.put("lb", "pound");
        aliases.put("lbs", "pound");
        aliases.put("pounds", "pound");
        aliases.put("lf", "leaf");
        aliases.put("lvs", "leaf");
        aliases.put("leaves", "leaf");
        aliases.put("lg", "large");
        aliases.put("lrg", "large");
        aliases.put("loaves", "loaf");
        aliases.put("links", "link");
        aliases.put("magnums", "magnum"); // = 52 fluid ounces
        aliases.put("measures", "measure");
        aliases.put("med", "medium");
        aliases.put("md", "medium");
        aliases.put("medium-size", "medium");
        aliases.put("medium-sized", "medium");
        aliases.put("mickies", "mickey"); // = 13 fluid ounces
        aliases.put("ounces", "ounce");
        aliases.put("oz", "ounce");
        aliases.put("packs", "pack");
        aliases.put("parts", "part");
        aliases.put("pats", "pat");
        aliases.put("pecks", "peck");
        aliases.put("pckg", "package");
        aliases.put("pkg", "package");
        aliases.put("pkt", "packet");
        aliases.put("pk", "package");
        aliases.put("pac", "package");
        aliases.put("packages", "package");
        aliases.put("packet", "packet");
        aliases.put("packets", "packet");
        aliases.put("pieces", "piece");
        aliases.put("pn", "pinch");
        aliases.put("pch", "pinch");
        aliases.put("pinches", "pinch");
        aliases.put("prt", "part");
        aliases.put("pt", "pint");
        aliases.put("pints", "pint");
        aliases.put("pts", "pint");
        aliases.put("pods", "pod");
        aliases.put("ponies", "pony");
        aliases.put("pouches", "pouch");
        aliases.put("portions", "portion");
        aliases.put("qrt", "quart");
        aliases.put("qrts", "quart");
        aliases.put("qt", "quart");
        aliases.put("qts", "quart");
        aliases.put("quarts", "quart");
        aliases.put("rk", "rack");
        aliases.put("racks", "rack");
        aliases.put("recipes", "recipe");
        aliases.put("ribs", "rib");
        aliases.put("rings", "ring");
        aliases.put("scoops", "scoop");
        aliases.put("sections", "section"); // = orange sections
        aliases.put("shakes", "shake");
        aliases.put("sht", "sheet");
        aliases.put("sheets", "sheet");
        aliases.put("shots", "shot");
        aliases.put("slabs", "slab");
        aliases.put("sl", "slice");
        aliases.put("slc", "slice");
        aliases.put("slices", "slice");
        aliases.put("sleeves", "sleeve");
        aliases.put("sm", "small");
        aliases.put("smidgens", "smidgen"); // = 1/32 tsp
        aliases.put("splashes", "splash");
        aliases.put("splits", "split");
        aliases.put("sprg", "sprig");
        aliases.put("sprigs", "sprig");
        aliases.put("sprinkles", "sprinkle");
        aliases.put("sq", "square");
        aliases.put("sqs", "square");
        aliases.put("squares", "square");
        aliases.put("st", "stalk");
        aliases.put("stalks", "stalk");
        aliases.put("sticks", "stick");
        aliases.put("strips", "strip");
        aliases.put("spoon", "tablespoon");
        aliases.put("spoonfull", "spoonful");
        aliases.put("spoonfuls", "spoonful");
        aliases.put("tablespoons", "tablespoon");
        aliases.put("tablespoonfuls", "tablespoonful");
        aliases.put("tablespoonsful", "tablespoonful");
        aliases.put("tablesp", "tablespoon");
        aliases.put("T", "tablespoon");
        aliases.put("tb", "tablespoon");
        aliases.put("tbl", "tablespoon");
        aliases.put("tblsp", "tablespoon");
        aliases.put("tbs", "tablespoon");
        aliases.put("tbsp", "tablespoon");
        aliases.put("tbsps", "tablespoon");
        aliases.put("tads", "tad"); // = 1/4 tsp
        aliases.put("tins", "tin");
        aliases.put("t", "teaspoon");
        aliases.put("ts", "teaspoon");
        aliases.put("teas", "teaspoon");
        aliases.put("teaspoons", "teaspoon");
        aliases.put("teaspoonsful", "teaspoonful");
        aliases.put("teaspoonfuls", "teaspoonful");
        aliases.put("thumb", "thumbs"); // of ginger
        aliases.put("tray", "trays"); // of ice cubes
        aliases.put("tsp", "teaspoon");
        aliases.put("tsps", "teaspoon");
        aliases.put("tubs", "tub"); // tub -> bæger
        aliases.put("tubes", "tube");
        aliases.put("twists", "twist");
        aliases.put("wedges", "wedge");
        aliases.put("wineglasses", "wineglass");
        aliases.put("wine glasses", "wineglass"); // 4 fluid ounces
        aliases.put("whl", "whole");
    }

    private static HashMap<String, String> pluralized = new HashMap<String, String>();
    static {
        pluralized.put("small", "small");
        pluralized.put("large", "large");
        pluralized.put("each", "each");
        pluralized.put("foot", "feet");
        pluralized.put("medium", "medium");
        pluralized.put("g", "g");
        pluralized.put("pinch", "pinches");
        pluralized.put("bunch", "bunches");
        pluralized.put("whole", "whole");
        pluralized.put("dozen", "dozen");
        pluralized.put("leaf", "leaves");
        pluralized.put("loaf", "loaves");
        pluralized.put("pony", "ponies");
        pluralized.put("pouch", "pouches");
        pluralized.put("splash", "splashes");
        pluralized.put("wineglass", "wineglasses");
        pluralized.put("kg", "kg");
        pluralized.put("ml", "ml");
        pluralized.put("cl", "cl");
        pluralized.put("dl", "dl");
        pluralized.put("l", "l");
        pluralized.put("lb", "lb");
        pluralized.put("oz", "oz");
    }

    // these units can both be part of the unit and part of the ingredient.
    // eg.
    // 2 cups small broccoli florets
    // 1 pound medium shrimp
    // 3/4 cup stick margarine, softened
    private static List<String> mustBeAloneUnits = new ArrayList<String>();
    static {
        mustBeAloneUnits.add("each");
        mustBeAloneUnits.add("small");
        mustBeAloneUnits.add("large");
        mustBeAloneUnits.add("medium");
        mustBeAloneUnits.add("stick");
        // mustBeAloneUnits.add("tub");
        mustBeAloneUnits.add("whole");
    }

    void normalize() {
        if (!Configuration.getBooleanProperty("EXPAND_UNIT_ABBREVIATIONS")) {
            return;
        }
        forceNormalize();
    }

    private static Pattern wordPattern = Pattern.compile("[a-zA-Z]+\\.*(?:\\(s\\))?");

    void forceNormalize() {
        name = name.trim();
        // normalize all words
        Matcher m = wordPattern.matcher(name);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(result, normalizeWord(m.group()));
        }
        m.appendTail(result);
        name = result.toString();
    }

    String normalizeWord(String name) {
        name = name.toLowerCase().trim();

        // remove a final '.' and '(s)' if any
        name = removeAbbrevPoint(name);

        if (aliases.containsKey(name)) {
            name = aliases.get(name);
        }

        return name;
    }

    // static Pattern removeAbbrevs = Pattern.compile("(\\.+|\\(s\\))$");
    private String removeAbbrevPoint(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }
        // remove dots
        int pos = name.length() - 1;
        while (name.charAt(pos) == '.') {
            pos--;
        }
        if (pos > 2 && name.substring(pos - 2, pos + 1).equals("(s)")) {
            pos -= 3;
        }
        name = name.substring(0, pos + 1);

        /*
         * Matcher m = removeAbbrevs.matcher(name); if (m.find()) { name =
         * m.replaceAll(""); }
         */
        return name;
    }

    /**
     * @return true, if it is a know unit.
     */

    // it should be the last word (otherwise "whole-wheat" would also be
    // accepted
    private static Pattern embeddedUnit = Pattern.compile("\\W(\\p{Alpha}+)$");

    public static boolean isKnown(String s) {
        return new Unit(s).isKnown();
    }

    /**
     * Is this a know unit? Is it in the alias list or if not does it contain an
     * embedded word that is in the list.
     * 
     * @return true if it is a know unit.
     */
    public boolean isKnown() {
        if (name == null || name.length() == 0)
            return false;

        String oldName = name;
        forceNormalize();

        // know unit?
        boolean known = isKnownSimple(name);
        name = oldName;
        if (known) {
            return true;
        }

        // if it contains an embedded unit
        Matcher matcher = embeddedUnit.matcher(name);
        if (matcher.find()) {
            return isKnownSimple(matcher.group(1));
        }

        return false;
    }

    public static boolean isKnownSimple(String str) {
        return aliases.containsKey(str) || aliases.values().contains(str);
    }

    /**
     * @return The pluralized version of the unit name.
     */
    public String pluralize() {
        if (name == null || name.length() == 0) {
            return "";
        }

        if (name.endsWith("s") || name.endsWith(")") || name.endsWith(".")) {
            return name;
        }

        if (pluralized.containsKey(name)) {
            return pluralized.get(name);
        }

        return name + "s";
    }

    public static boolean mustBeAloneUnit(String name) {
        return mustBeAloneUnits.contains(name.trim());
    }

    /**
     * @return true if it is a weight unit.
     */
    public boolean isWeight() {
        String oldName = name;
        forceNormalize();

        // TODO: fast hack.
        boolean result = ("pound".equals(name) || "ounce".equals(name) || "g".equals(name) || "kg".equals(name));
        name = oldName;

        return result;
    }

    /**
     * @return true if it is a volumen unit.
     */
    public boolean isVolumen() {
        String oldName = name;
        forceNormalize();

        // TODO: fast hack.
        boolean result = ("ml".equals(name) || "cl".equals(name) || "dl".equals(name) || "l".equals(name));
        name = oldName;

        return result;
    }

    // eg.
    // a handful
    private static String UNIT_MODIFIERS = "(?:\\(?(?:a|big|generous|heavy|heaping|large|level|medium|rounded|scant|small)\\)?\\s+)";
    private static String UNIT_NAME = "(?:(\\s*\\w+(?:\\(s\\))?)\\s)";

    // The NEED to be a space after the unit name to NOT make it match
    // 4 medium-apples
    // 4 medium, ripe apples
    private static Pattern UNIT_PATTERN = Pattern.compile("^\\s*(" + // group 1
            // group 2,3: (description) optional-unit
            " (\\(.*?\\))" + // the description in ()
            "   " + UNIT_NAME + "?" + // an optional unit after the description
            // group 3,5: a simple description without (): 14-ounce can
            "| ([\\d\\.\\,/]+[\\s\\-]+\\w+)" + // a simple description without
                                               // (): 14-ounce can
            "   " + UNIT_NAME + "?" + // an optional unit after the description
            // group 6,7: A unit followed by a description: 1 can(s) (15-ounce)
            "|   (can(?:\\(s\\))?)\\s*" + // an unit
            " (\\([\\d\\.\\,]+[\\s\\-]+[\\w\\.]+\\s*\\))" +
            // group 8,9: a simple unit with an optional (s)
            "|  (" + UNIT_MODIFIERS + ")?([a-z\\.\\-]+(?:\\(s\\))?(?:\\s|$))" + ")(\\s*\\w+\\.*\\s)?", // group
                                                                                                       // 10:
                                                                                                       // and
                                                                                                       // the
                                                                                                       // following
                                                                                                       // word
            Pattern.CASE_INSENSITIVE + Pattern.COMMENTS);

    /**
     * Extract the unit from the given ingredient line (with no amount!). It
     * must start with the unit name.
     * 
     * @param ingredientLine
     * @return
     */
    public static Unit extract(String ingredientLine) {
        Matcher m = UNIT_PATTERN.matcher(ingredientLine);
        if (!m.find())
            return null;

        String[] parts = new String[2];
        boolean withDescription = false;
        boolean reverseDescription = false;
        // simple unit with modifier ?
        if (m.group(2) != null) { // with description in ()
            parts[0] = m.group(2);
            parts[1] = m.group(3) == null ? "" : m.group(3);
            withDescription = true;
        } else if (m.group(4) != null) { // simple description : 14-ounce can
            parts[0] = m.group(4);
            parts[1] = m.group(5) == null ? "" : m.group(5);
            withDescription = true;
        } else if (m.group(7) != null) {
            parts[0] = m.group(6) == null ? "" : m.group(6);
            parts[1] = m.group(7);
            reverseDescription = true;
        } else { // an optional modifier and a simple unit with an optional (s)
            parts[0] = m.group(8) == null ? "" : m.group(8);
            parts[1] = m.group(9) == null ? "" : m.group(9);
        }

        // if there is no unit following the () then the parenthesis
        // needs to contain at least one unit. To separate
        // (some ecological) sugar
        // (2 oz) flank steak
        // (center-cut) slice ham
        if (withDescription && !containsUnit(parts[0]) && parts[1].length() == 0) {
            parts[0] = "";
            parts[1] = "";
        }

        // check if the last word after the modifier is a unit or not
        // to handle "1 small onion" and "1 small cup sugar"
        String unitString = "";
        if (reverseDescription || Unit.isKnown(parts[1])) {
            unitString = parts[0].trim() + " " + parts[1].trim();
        } else {
            unitString = parts[0];
        }

        // is the next word also a know unit? eg. "10 ounce can sugar"
        String nextWord = m.group(10) == null ? null : m.group(10).trim();
        if (nextWord != null && Unit.isKnown(parts[1]) && Unit.isKnown(nextWord) && !mustBeAloneUnit(nextWord)) {
            unitString = unitString.trim() + " " + m.group(10).trim();
        }

        return new Unit(unitString.trim());
    }

    /**
     * Test if the given string contains one valid unit name
     * 
     * @param str
     * @return
     */
    public static boolean containsUnit(String str) {
        boolean isKnown = false;
        str = str.trim();
        // normalize all words
        Matcher m = wordPattern.matcher(str);
        while (m.find()) {
            isKnown |= isKnown(m.group());
        }
        return isKnown;
    }

}
