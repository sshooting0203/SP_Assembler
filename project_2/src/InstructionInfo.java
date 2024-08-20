public class InstructionInfo {
    private String _name;
    private int _opcode;
    private int _numberOfOperand;
    private int _format;
    public InstructionInfo(String line) throws RuntimeException {
        String[] parts = line.split("\\s+");
        if (parts.length != 4) {
            throw new RuntimeException("Wrong file format");
        }
        _name = parts[0];
        _numberOfOperand = getFormatCode(parts[1]);
        _opcode = Integer.parseInt(parts[3],16);
        _format = Integer.parseInt(parts[2]);
    }
    public String getName() { return _name; }
    public int getOpcode() {
        return _opcode;
    }
    public int getNumberOfOperand() {
        return _numberOfOperand;
    }
    public int getFormat() {
        return _format;
    }
    private int getFormatCode(String formatStr) {
        switch (formatStr) {
            case "M":
                return 1;
            case "R":
                return 1;
            case "RR":
                return 2;
            case "RN":
                return 1;
            case "-":
                return 0;
            case "N":
                return 1;
            default:
                throw new IllegalArgumentException("Wrong format code : " + formatStr);
        }
    }
}