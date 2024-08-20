public class InstructionInfo {
	/**
	 * 기계어 목록 파일의 한 줄을 읽고, 이를 파싱하여 저장한다.
	 *
	 * @param line 기계어 목록 파일의 한 줄
	 * @throws RuntimeException 잘못된 파일 형식
	 */
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

	/**
	 * 기계어 명칭을 반환한다.
	 *
	 * @return 기계어 명칭
	 */
	public String getName() {
		return _name;
	}

	/**
	 * 기계어의 opcode를 반환한다.
	 *
	 * @return 기계어의 opcode
	 */
	public int getOpcode() {
		return _opcode;
	}

	/**
	 * 기계어의 operand 개수를 반환한다.
	 *
	 * @return 기계어의 operand 개수
	 */
	public int getNumberOfOperand() {
		return _numberOfOperand;
	}

	/**
	 * 기계어의 형식을 반환한다.
	 *
	 * @return 기계어의 형식. 1: ~~, 2: ~~, 3: ~~, 4:~~, 5:~~ (본인의 사용 방식에 맞춰 작성한다)
	 */
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