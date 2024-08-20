import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceManager {
    private byte[] memory; // 메모리로 사용
    private int[] registers; // 레지스터로 사용
    public ResourceManager() {
        memory = new byte[1 << 20]; // 2^20 => SIC/XE
        registers = new int[10];
    }

    public void setMemory(int address, byte value) {
        if (address >= 0 && address < memory.length) {
            memory[address] = value;
        } else {
            System.err.println("Invalid memory address: " + address);
        }
    }
    public byte getMemory(int address) {
        if (address >= 0 && address < memory.length) {
            return memory[address];
        } else {
            throw new IllegalArgumentException("Invalid memory address: " + address);
        }
    }

    public void setRegister(int index, int value) {
        registers[index] = value;
    }
    public int getRegister(int index) {
        return registers[index];
    }
}
