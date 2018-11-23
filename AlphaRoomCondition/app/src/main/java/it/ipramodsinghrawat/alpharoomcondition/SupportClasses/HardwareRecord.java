package it.ipramodsinghrawat.alpharoomcondition.SupportClasses;

public class HardwareRecord {
    //Fire Extinguisher System,Air Purifier,Air Condition,Air Vents,Air Exhaust System

    /*ToDo: keep fetching hardware status on regular interval only post data in event of Air Condition
    * with other old data depend upon time*/

    private int fireExtinguisherSystem;
    private int airPurifier;
    private int airCondition;
    private int airVents;
    public int airExhaustSystem;
    String dateTime;

    public int getFireExtinguisherSystem() {
        return fireExtinguisherSystem;
    }

    public void setFireExtinguisherSystem(int fireExtinguisherSystem) {
        this.fireExtinguisherSystem = fireExtinguisherSystem;
    }

    public int getAirCondition() {
        return airCondition;
    }

    public void setAirCondition(int airCondition) {
        this.airCondition = airCondition;
    }

    public int getAirVents() {
        return airVents;
    }

    public void setAirVents(int airVents) {
        this.airVents = airVents;
    }

    public int getAirExhaustSystem() {
        return airExhaustSystem;
    }

    public void setAirExhaustSystem(int airExhaustSystem) {
        this.airExhaustSystem = airExhaustSystem;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getAirPurifier() {
        return airPurifier;
    }

    public void setAirPurifier(int airPurifier) {
        this.airPurifier = airPurifier;
    }

    public HardwareRecord(int fireExtinguisherSystem, int airPurifier, int airCondition,
                          int airVents, int airExhaustSystem,
                          String dateTime){

        this.fireExtinguisherSystem = fireExtinguisherSystem;
        this.airPurifier = airPurifier;
        this.airCondition = airCondition;
        this.airVents = airVents;
        this.airExhaustSystem = airExhaustSystem;
        this.dateTime = dateTime;

    }
}
