package Database.Tuples;

abstract class AbstractTuple implements Tuple {
    protected tupleType tupleName;

    public tupleType getTupleName() {
        return tupleName;
    }

    public void setTupleName(tupleType tupleName) {
        this.tupleName = tupleName;
    }
}
