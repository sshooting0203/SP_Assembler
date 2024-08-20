import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

// SicXeSimulator.java
public class SicXeSimulator {
    private ResourceManager resourceManager;
    private ObjectCodeLoader loader;
    private InstructionExecutor executor;
    private int EXECADDR; // 실행 시작 주소

    public SicXeSimulator() {
        resourceManager = new ResourceManager();
        loader = new ObjectCodeLoader();
        executor = new InstructionExecutor();
        EXECADDR=0;
    }
    public void loadObjectCode(String objectCode) throws Exception {
        EXECADDR = loader.loadObjectCode(objectCode, resourceManager);
    }
    public ArrayList<TextInfo> executeInstruction() throws Exception {
        executor.executeInstruction(EXECADDR,resourceManager);
        return executor.getTextInfos();
    }
    public ArrayList<String> getHeader(){
        ArrayList<String> newHeader = new ArrayList<>();
        newHeader.add(loader.getProgrameName());
        newHeader.add(loader.getStartAddress());
        newHeader.add(loader.getProgramLen());
        newHeader.add(loader.getExecuteAddress());
        return newHeader;
    }
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
