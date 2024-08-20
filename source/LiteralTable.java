import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;


public class LiteralTable {
	/**
	 * 리터럴 테이블을 초기화한다.
	 */
	public LiteralTable() {
		literalMap = new LinkedHashMap<String, Literal>();
	}

	/**
	 * 리터럴을 리터럴 테이블에 추가한다.
	 *
	 * @param literal 추가할 리터럴
	 * @throws RuntimeException 비정상적인 리터럴 서식
	 */
	public void putLiteral(String literal) throws RuntimeException {
		// TODO: 리터럴을 literalMap에 추가하기.
		String value = compute_value(literal);
		if(this.searchLiteral(literal).isEmpty()){
			Literal literalInfo = new Literal(literal,value);
			literalMap.put(literal,literalInfo);
		}
	}

	// TODO: 추가로 필요한 method 구현하기.
	public String gatherValue(int prev_locctr,int locctr){
		StringBuilder result = new StringBuilder();
		for (Literal literal : literalMap.values()) {
			if(literal.getAddress().orElse(-1)<=locctr &&literal.getAddress().orElse(-1)> prev_locctr){
				literal.get_value();
				literal.get_value().ifPresent(value -> result.append(value));
			}else{
				int decimalNumber = literal.get_value().map(Integer::parseInt).orElse(0);
				result.append(String.format("%06X", decimalNumber));
			}
		}
		return result.toString();
	}
	public Optional<Literal> searchLiteral(String literal) {
		return Optional.ofNullable(literalMap.get(literal));
	}
	public Optional<Integer> getAddress(String literalName){
		Optional<Literal> optLiteral = searchLiteral(literalName);
		return optLiteral.get().getAddress();
	}
	public int putAddress(int address){
		// literal의 주소 계산하는 메서드
		int literalMap_len = 0;
		if(literalMap.isEmpty()){
			return address;
		}
		for (Literal literalInfo : literalMap.values()) {
			int address_value = literalInfo.getAddress().orElse(-1);
			if(address_value==-1){
				literalInfo.set_address(Optional.of(address));
				String literal_str = literalInfo.getLiteral();
				int literal_len = lengthInQuotes(literal_str);
				if(literal_len==0){
					literal_len=3;
				}

				if(literal_str.charAt(1)=='X'){
					literal_len /=2;
				}
				address += literal_len;

			}
		}
		return address;
		//return address+literalMap_len;
	}
	public static int lengthInQuotes(String operandStr) {
		int length = 0;
		boolean inQuotes = false;
		for (int i = 0; i < operandStr.length(); i++) {
			char ch = operandStr.charAt(i);
			if (ch == '\'') {
				inQuotes = !inQuotes;
			} else if (inQuotes) {
				length++;
			}
		}
		return length;
	}
	public String compute_value(String operand){
		int startIndex = operand.indexOf('\'');
		int endIndex = operand.lastIndexOf('\'');
		// 작은 따옴표 사이의 문자 추출
		String new_str;
		if(startIndex==-1 || endIndex==-1){
			startIndex = 0;
			new_str = operand.substring(startIndex + 1);
		}else{
			new_str = operand.substring(startIndex + 1, endIndex);
		}
		if(operand.charAt(1)=='X'){
			return new_str;
		}else if(operand.charAt(1)=='C'){
			StringBuilder asciiStr = new StringBuilder();
			for (char c : new_str.toCharArray()) {
				// 각 문자의 아스키 코드를 문자열에 추가
				asciiStr.append(Integer.toHexString((int) c));
			}
			return asciiStr.toString();
		}else{
			int decimalInt = Integer.parseInt(new_str);
			String hexStr = Integer.toHexString(decimalInt);
			while (hexStr.length() < 6) {
				hexStr = "0" + hexStr;
			}
			return hexStr;
		}
	}
	/**
	 * 리터럴 테이블을 String으로 변환한다.
	 */
	@Override
	public String toString() {
		// TODO: 구현하기. Literal 객체의 toString을 활용하자.
		if(!literalMap.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for (Literal literal : literalMap.values()) {
				builder.append(literal.toString());
			}
			return builder.toString();
		}else {
			return "";
		}
	}
	public boolean isLiteralMapEmpty() {
		return literalMap.isEmpty();
	}

	/** 리터럴 맵. key: 리터럴 String, value: 리터럴 객체 */
	private HashMap<String, Literal> literalMap;
}

class Literal {
	/**
	 * 리터럴 객체를 초기화한다.
	 *
	 * @param literal 리터럴 String
	 */
	public Literal(String literal,String value) {
		// TODO: 리터럴 객체 초기화.
		_literal = literal;
		_value = Optional.ofNullable(value);
		_address = Optional.of(-1);
	}

	/**
	 * 리터럴 String을 반환한다.
	 *
	 * @return 리터럴 String
	 */
	public String getLiteral() {
		return _literal;
	}

	/**
	 * 리터럴의 주소를 반환한다. 주소가 지정되지 않은 경우, Optional.empty()를 반환한다.
	 *
	 * @return 리터럴의 주소
	 */
	public Optional<Integer> getAddress() {
		return _address;
	}

	// TODO: 추가로 선언한 field에 대한 getter 작성하기.
	public Optional<String> get_value() {
		return _value;
	}

	public void set_address(Optional<Integer> _address) {
		this._address = _address;
	}

	/**
	 * 리터럴을 String으로 변환한다. 리터럴의 address에 관한 정보도 리턴값에 포함되어야 한다.
	 */
	@Override
	public String toString() {
		// TODO: 리터럴을 String으로 표현하기.
		StringBuilder builder = new StringBuilder();
		builder.append(_literal).append(" ");
		builder.append(String.format("%X", _address.orElse(0))).append(" ");
		//builder.append(_value).append(" ");
		return builder.toString();
	}
	/** 리터럴 String */
	private String _literal;

	/** 리터럴 주소. 주소가 지정되지 않은 경우 empty */
	private Optional<Integer> _address;

	// TODO: 추가로 필요한 field 선언하기.
	/** 리터럴 값 Value */
	private Optional<String> _value;
}