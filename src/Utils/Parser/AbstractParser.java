package Utils.Parser;

abstract class AbstractParser implements Parser {
    protected parserType parserName;

    public parserType getParserName() {
        return parserName;
    }

    public void setParserName(parserType parserName) {
        this.parserName = parserName;
    }
}
