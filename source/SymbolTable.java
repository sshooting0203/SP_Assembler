import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

public class SymbolTable {
	/**
	 * 심볼 테이블 객체를 초기화한다.
	 */
	public SymbolTable() {
		symbolMap = new LinkedHashMap<String, Symbol>();
		referMap = new LinkedHashMap<String, Symbol>();
	}
	/*
	 * 명령어가 CSECT인 경우
	 */
	public void putLabel(String label, int address) throws RuntimeException{
		Optional<Symbol> existingSymbol = searchSymbol(label);
		if (!existingSymbol.isEmpty()) {
			throw new RuntimeException("Duplicate symbol: " + label);
		}
		Symbol symbol = new Symbol(label,address);
		symbolMap.put(label, symbol);
	}
	/**
	 * EQU를 제외한 명령어/지시어에 label이 포함되어 있는 경우, 해당 label을 심볼 테이블에 추가한다.
	 * 
	 * @param label   라벨
	 * @param address 심볼의 주소
	 * @throws RuntimeException (TODO: exception 발생 조건을 작성하기)
	 */
	public void putLabel(String label, int address,String control_section) throws RuntimeException {
		// TODO: EQU를 제외한 명령어/지시어의 label로 생성되는 심볼을 추가하기.

		Optional<Symbol> existingSymbol = searchSymbol(label);
		if (!existingSymbol.isEmpty()) {
			throw new RuntimeException("Duplicate symbol: " + label);
		}
		Symbol symbol = new Symbol(label, address, control_section);

		// 심볼 맵에 추가
		symbolMap.put(label, symbol);
	}

	/**
	 * EQU에 label이 포함되어 있는 경우, 해당 label을 심볼 테이블에 추가한다.
	 * 
	 * @param label    라벨
	 * @param locctr   locctr 값
	 * @param equation equation 문자열
	 * @throws RuntimeException equation 파싱 오류
	 */
	public void putLabel(String label, int locctr, String equation,String control_section) throws RuntimeException {
		// TODO: EQU의 label로 생성되는 심볼을 추가하기.

		int address;
		Optional<Symbol> existingSymbol = searchSymbol(label);
		if (!existingSymbol.isEmpty()) {
			throw new RuntimeException("Duplicate symbol: " + label);
		}

		if(equation.equals("*")){
			// EQU의 operand가 "*"인 경우
			address = locctr;
			Symbol symbol = new Symbol(label, address, equation, control_section);
			symbolMap.put(label, symbol);
		}else{
			// EQU의 operand가 expression인 경우
			address = calculateAddress(equation);
			// symtab에 추가할 address를 계산
			Symbol symbol = new Symbol(label, address);
			symbolMap.put(label, symbol);
		}

	}

	/**
	 * EXTREF에 operand가 포함되어 있는 경우, 해당 operand를 심볼 테이블에 추가한다.
	 * 
	 * @param refers operand에 적힌 하나의 심볼
	 * @throws RuntimeException (TODO: exception 발생 조건을 작성하기)
	 */
	public void putRefer(ArrayList<String> refers) throws RuntimeException {
		// TODO: EXTREF의 operand로 생성되는 심볼을 추가하기.
		for (String refer_str : refers) {
			refer_str = refer_str.trim();
			Symbol symbol = new Symbol(refer_str);
			referMap.put(refer_str, symbol);
		}
	}
	/*
	 * symbolMap의 마지막 부분에 referMap을 추가하는 메서드
	 */
	public void linkwithRefer(){
		symbolMap.putAll(referMap);
	}
	/**
	 * 심볼 테이블에서 심볼을 찾는다.
	 * 
	 * @param name 찾을 심볼 명칭
	 * @return 심볼. 없을 경우 empty
	 */
	public Optional<Symbol> searchSymbol(String name) {
		// TODO: symbolMap에서 name에 해당하는 심볼을 찾아 반환하기.
		return Optional.ofNullable(symbolMap.get(name));
	}

	/**
	 * 심볼 테이블에서 심볼을 찾아, 해당 심볼의 주소를 반환한다.
	 * 
	 * @param symbolName 찾을 심볼 명칭
	 * @return 심볼의 주소. 없을 경우 empty
	 */
	public Optional<Integer> getAddress(String symbolName) {
		Optional<Symbol> optSymbol = searchSymbol(symbolName);
		return optSymbol.map(s -> s.getAddress());
	}

	private int calculateAddress(String equation) throws  RuntimeException{
		// TODO: EQU에서의 주소 계산 로직 작성
		String[] parts = equation.split("[+-]");
		int result = 0;

		for (String part : parts) {
			String symbolName = part.trim();
			Optional<Integer> optAddress = this.getAddress(symbolName);

			if (optAddress.isPresent()) {
				// 심볼이 존재하는 경우 해당 심볼의 주소를 더하거나 뺌
				int address = optAddress.get();
				if (equation.indexOf(part) > 0 && equation.charAt(equation.indexOf(part) - 1) == '-') {
					result -= address;
				} else {
					result += address;
				}
			} else {
				// 심볼이 존재하지 않는 경우 예외 처리
				throw new RuntimeException("Symbol not found in symbol table: " + symbolName);
			}
		}
		return result;
	}
	/**
	 * 심볼 테이블을 String으로 변환한다. Assembler.java에서 심볼 테이블을 출력하기 위해 사용한다.
	 */
	@Override
	public String toString() {
		// TODO: 심볼 테이블을 String으로 표현하기. Symbol 객체의 toString을 활용하자.
		if(!symbolMap.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for (Symbol symbol : symbolMap.values()) {
				builder.append(symbol.toString()).append("\n");
			}
			return builder.toString();
		}
		else{
			return "";
		}
	}

	/** 심볼 테이블. key: 심볼 명칭, value: 심볼 객체 */
	private HashMap<String, Symbol> symbolMap;
	private HashMap<String, Symbol> referMap;

}

class Symbol {
	/**
	 * 심볼 객체를 초기화한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼의 절대 주소
	 */
	public Symbol(String name, int address, Optional<String> equation, Optional<String> control_section) {
		// TODO: 심볼 객체 초기화.
		_name = name;
		_address = address;
		_equation = equation;
		_control_section = control_section;
	}
	public Symbol(String name, int address ,String equation,String control_section){ // EQU의 operand가 *인 경우, symbolMap
		// equation이 유효
		this(name,address,Optional.of(equation), Optional.ofNullable(control_section));
	}
	public Symbol(String name, int address, String control_section){ // EQU를 제외한 경우, symbolMap
		this(name,address,Optional.empty(), Optional.ofNullable(control_section));
	}
	public Symbol(String name, int address){ // CSECT이거나 EQU의 operand가 expr인 경우, symbolMap
		this(name,address,Optional.empty(), Optional.empty());
	}
	public Symbol(String refer) { // referMap에 사용
		this(refer+" REF",-1, Optional.empty(), Optional.empty());
	}

	/**
	 * 심볼 명칭을 반환한다.
	 * 
	 * @return 심볼 명칭
	 */
	public String getName() {
		return _name;
	}

	/**
	 * 심볼의 주소를 반환한다.
	 * 
	 * @return 심볼 주소
	 */
	public int getAddress() {
		return _address;
	}

	// TODO: 추가로 선언한 field에 대한 getter 작성하기.

	public Optional<String> getEquation() {
		return _equation;
	}
	public Optional<String> get_control_section() {
		return _control_section;
	}
	/**
	 * 심볼을 String으로 변환한다.
	 */
	@Override
	public String toString() {
		// TODO: 심볼을 String으로 표현하기.
		StringBuilder builder = new StringBuilder();
		builder.append(_name).append(" ");
		if (_address!=-1){
			builder.append("0x").append(String.format("%04X", _address)).append(" ");
		}
		//builder.append("Equation: ").append(_equation.orElse("N/A")).append(", ");
		builder.append(_control_section.orElse(""));
		return builder.toString();
	}
	/** 심볼의 명칭 */
	private String _name;

	/** 심볼의 주소 */
	private int _address;

	// TODO: 추가로 필요한 field 선언
	/** 심볼의 expression 저장가능 */
	private Optional<String> _equation;
	/** 심볼의 control_section */
	private Optional<String> _control_section;
}