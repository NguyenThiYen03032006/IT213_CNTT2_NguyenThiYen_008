package vn.rikkei.exam.clinicappointment.service.chat;

import java.util.ArrayList;
import java.util.List;

public class ToolExecutionTracker {

    private static final ThreadLocal<List<String>> CURRENT_TOOLS = ThreadLocal.withInitial(ArrayList::new);

    public static void record(String toolName) {
        if (toolName != null && !toolName.isBlank()) {
            List<String> list = CURRENT_TOOLS.get();
            if (!list.contains(toolName)) {
                list.add(toolName);
            }
        }
    }

    public static List<String> getToolsUsed() {
        return new ArrayList<>(CURRENT_TOOLS.get());
    }

    public static void clear() {
        CURRENT_TOOLS.remove();
    }
}
