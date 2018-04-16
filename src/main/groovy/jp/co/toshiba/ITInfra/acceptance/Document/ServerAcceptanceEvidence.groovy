package jp.co.toshiba.ITInfra.acceptance.Document;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FileUtils.*
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import net.sf.jett.transform.ExcelTransformer;

public class ServerAcceptanceEvidence {
    String system;
	List<ServerAcceptanceConfigs> configs = new ArrayList<ServerAcceptanceConfigs>();
}

class ServerAcceptanceConfigs {
    String itemName;
    Double unitCost;
    Double quantity;

    String system;
    String model;
    String user;
    String password;
    String ui_type;
    String os_type;
    String os_version;
    String cpu_size;
    String memory_size;
    String raid_config;
    String disk_size;
    String disk_partition;
    String disk_partition_size;
    String managed_hostname;
    String managed_ip;
    String managed_subnet;
    String managed_gateway;
    String nic1_hostname;
    String nic1_ip;
    String nic2_hostname;
    String nic2_ip;
    String nic3_hostname;
    String nic3_ip;
    String nic4_hostname;
    String nic4_ip;
    String nic5_ip;
    String nic6_ip;
    String nic7_ip;
    String nic8_ip;
    String ip_subnet;
    String ip_gateway;
    String ip_ntp;
    String ip_hostname_top;
    String ip_address_top;
    String ip_hostname_lower;
    String ip_address_lower;
    String ip_hostname_local;
    String ip_address_local;
    String package_list;
}