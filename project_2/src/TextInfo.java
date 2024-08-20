public class TextInfo {
    private int valueA;
    private int valueX;
    private int valueL;
    private int valuePC;
    private int valueSW;
    // SIC register
    private int valueB;
    private int valueS;
    private int valueT;
    private int valueF;

    // SIC/XE register
    String currentInst;
    String currenLog;
    String testDevice;

    public String getTestDevice() {
        return testDevice;
    }
    public void setTestDevice(String testDevice) {
        this.testDevice = testDevice;
    }
    public String getCurrentInst() {
        return currentInst;
    }
    public String getCurrenLog() {
        return currenLog;
    }
    public void setCurrentInst(String currentInst) {
        this.currentInst = currentInst;
    }
    public void setCurrenLog(String currenLog) {
        this.currenLog = currenLog;
    }

    public void setValueA(int valueA) {
        this.valueA = valueA;
    }

    public void setValueX(int valueX) {
        this.valueX = valueX;
    }

    public void setValueL(int valueL) {
        this.valueL = valueL;
    }

    public void setValuePC(int valuePC) {
        this.valuePC = valuePC;
    }

    public void setValueSW(int valueSW) {
        this.valueSW = valueSW;
    }

    public void setValueB(int valueB) {
        this.valueB = valueB;
    }

    public void setValueS(int valueS) {
        this.valueS = valueS;
    }

    public void setValueT(int valueT) {
        this.valueT = valueT;
    }

    public void setValueF(int valueF) {
        this.valueF = valueF;
    }

    public int getValueA() {
        return valueA;
    }

    public int getValueX() {
        return valueX;
    }

    public int getValueL() {
        return valueL;
    }

    public int getValuePC() {
        return valuePC;
    }

    public int getValueSW() {
        return valueSW;
    }

    public int getValueB() {
        return valueB;
    }

    public int getValueS() {
        return valueS;
    }

    public int getValueT() {
        return valueT;
    }

    public int getValueF() {
        return valueF;
    }


}
