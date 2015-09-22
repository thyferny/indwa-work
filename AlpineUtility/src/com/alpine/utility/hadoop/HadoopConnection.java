/**
 * ClassName HadoopConnection.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

import com.alpine.utility.file.FileUtility;
import com.alpine.utility.file.StringUtil;

/***
 *
 * @author John Zhao
 *
 */
public class HadoopConnection {

    private static Logger itsLogger = Logger.getLogger(HadoopConnection.class);
    public static final String file_suffix = ".hdc";
    public static final String KEY_CONNNAME = "connName";
    public static final String KEY_HDFS_HOSTNAME = "hdfsHostname";
    public static final String KEY_HDFS_PORT = "hdfsPort";
    public static final String KEY_JOB_HOSTNAME = "jobHostname";
    public static final String KEY_JOB_PORT = "jobPort";
    public static final String KEY_VERSION = "hadoopVersion";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_GROUPNAME = "groupName";
    public static final String KEY_SECURITY_MODE = "securityMode";
    public static final String KEY_HDFS_PRINCIPAL = "hdfsPrincipal";
    public static final String KEY_HDFS_KEYTAB = "hdfsKeyTab";
    public static final String KEY_MAPRED_PRINCIPAL = "mapredPrincipal";
    public static final String KEY_MAPRED_KEYTAB = "mapredKeyTab";
    public static final String SECURITY_MODE_SIMPLE = "simple";
    public static final String SECURITY_MODE_KERBEROS = "kerberos";
    public static String CURRENT_HADOOP_VERSION = HadoopConstants.VERSION_APACHE_HADOOP_0_20_2;

    static {
        try {
            InputStream rs = HadoopConnection.class.getResourceAsStream("/com/alpine/utility/hadoop/hadoop_version.properties");
            if (rs != null) {
                CURRENT_HADOOP_VERSION = FileUtility.readStreamToString(rs).toString();
                CURRENT_HADOOP_VERSION = CURRENT_HADOOP_VERSION.trim();
            }
        } catch (Exception e) {}
    }

    private String connName = null; // name of the connection
    private String securityMode = HadoopConnection.SECURITY_MODE_SIMPLE; // simple or kerberos
    private String hdfsPrincipal = null;
    private String hdfsKeyTab = null;
    private String mapredPrincipal = null;
    private String mapredKeyTab = null;
    private String userName = null;
    private String groupName = null;
    private int hdfsPort;
    private String version;
    private String jobHostName;
    private int jobPort;


    public HadoopConnection(String connName, String userName, String groupName,
                            String hdfsHostName, int hdfsPort, String version,
                            String jobHostName, int jobPort, String securityMode,
                            String hdfsPrincipal, String hdfsKeyTab, String mapredPrincipal,
                            String mapredKeyTab) {

        this.connName = connName;
        this.groupName = groupName;
        this.userName = userName;
        this.hdfsHostName = hdfsHostName;
        this.hdfsPort = hdfsPort;
        this.version = version.trim();
        this.jobHostName = jobHostName;
        this.jobPort = jobPort;
        this.securityMode = securityMode;
        this.hdfsPrincipal = hdfsPrincipal;
        this.hdfsKeyTab = hdfsKeyTab;
        this.mapredPrincipal = mapredPrincipal;
        this.mapredKeyTab = mapredKeyTab;

    }


    public HadoopConnection(Properties props) {

        this.hdfsHostName = props.get(KEY_HDFS_HOSTNAME).toString();
        this.hdfsPort = (Integer.parseInt(props.get(KEY_HDFS_PORT).toString()));
        this.version = (props.get(KEY_VERSION).toString()).trim();
        this.jobHostName = props.get(KEY_JOB_HOSTNAME).toString();
        this.jobPort = (Integer.parseInt(props.get(KEY_JOB_PORT).toString()));

        this.connName = props.get(KEY_CONNNAME).toString();

        this.userName = props.get(KEY_USERNAME).toString();
        this.groupName = props.get(KEY_GROUPNAME).toString();
        if (props.get(KEY_SECURITY_MODE) != null) {
            this.securityMode = props.get(KEY_SECURITY_MODE).toString();
        }

        if (props.get(KEY_HDFS_PRINCIPAL) != null) {
            this.hdfsPrincipal = props.get(KEY_HDFS_PRINCIPAL).toString();
        }
        if (props.get(KEY_HDFS_KEYTAB) != null) {
            this.hdfsKeyTab = props.get(KEY_HDFS_KEYTAB).toString();
        }
        if (props.get(KEY_MAPRED_KEYTAB) != null) {
            this.mapredKeyTab = props.get(KEY_MAPRED_KEYTAB).toString();
        }
        if (props.get(KEY_MAPRED_PRINCIPAL) != null) {
            this.mapredPrincipal = props.get(KEY_MAPRED_PRINCIPAL).toString();
        }
    }

    public Configuration toHadoopConfiguration() {
        Configuration conf = new Configuration();
        conf.setInt("ipc.client.connect.max.retries.on.timeouts", 1);
        conf.setInt("ipc.client.connection.maxidletime", 5000); // 5s
        conf.setInt("ipc.client.connect.max.retries", 1);
        fillSecurityInfo2Config(conf);

        conf.set(HadoopConstants.PROPERTY_FS_NAME, HadoopConstants.HDFS_PREFIX
                + getHdfsHostName() + ":" + getHdfsPort());
        conf.set(HadoopConstants.PROPERTY_JOB_UGI, getUserName() + ","
                + getGroupName());

        return conf;

    }

    public void fillSecurityInfo2Config(Configuration conf) {
        if (HadoopConnection.SECURITY_MODE_KERBEROS.equals(this
                .getSecurityMode())) {
            conf.set("hadoop.security.authentication", SECURITY_MODE_KERBEROS);
            conf.set("dfs.namenode.kerberos.principal", this.getHdfsPrincipal());
            conf.set("dfs.datanode.kerberos.principal", this.getHdfsPrincipal());
            conf.set("mapreduce.jobtracker.kerberos.principal",
                    this.getMapredPrincipal());
            conf.set("mapreduce.tasktracker.kerberos.principal",
                    this.getMapredPrincipal());

            conf.set("dfs.namenode.keytab.file", this.getHdfsKeyTab());
            conf.set("dfs.datanode.keytab.file", this.getHdfsKeyTab());
            conf.set("mapreduce.jobtracker.keytab.file", this.getMapredKeyTab());
            conf.set("mapreduce.tasktracker.keytab.file",
                    this.getMapredKeyTab());
            // here have to do this since this method does not exist in apache
            // 0.20.2
            SetConf4UserGroupInformation(conf);
            // UserGroupInformation.setConfiguratio(conf);

        }
    }

    public static void SetConf4UserGroupInformation(Configuration conf) {
        Method[] methods = UserGroupInformation.class.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("setConfiguration")
                    && methods[i].getParameterTypes()[0]
                    .equals(Configuration.class)) {
                try {
                    methods[i].invoke(UserGroupInformation.class, conf);
                } catch (Exception e) {
                    // nothing to do
                }
            }
        }
    }

    public void fillSecurityInfo2Props(Properties properties) {
        if (HadoopConnection.SECURITY_MODE_KERBEROS.equals(this
                .getSecurityMode())) {
            properties.put("hadoop.security.authentication",
                    SECURITY_MODE_KERBEROS);
            properties.put("dfs.namenode.kerberos.principal",
                    this.getHdfsPrincipal());
            properties.put("dfs.datanode.kerberos.principal",
                    this.getHdfsPrincipal());
            properties.put("mapreduce.jobtracker.kerberos.principal",
                    this.getMapredPrincipal());
            properties.put("mapreduce.tasktracker.kerberos.principal",
                    this.getMapredPrincipal());

            properties.put("dfs.namenode.keytab.file", this.getHdfsKeyTab());
            properties.put("dfs.datanode.keytab.file", this.getHdfsKeyTab());
            properties.put("mapreduce.jobtracker.keytab.file",
                    this.getMapredKeyTab());
            properties.put("mapreduce.tasktracker.keytab.file",
                    this.getMapredKeyTab());

        }

    }


    public static ClusterStatus getClusterInfo(HadoopConnection conn)
            throws IOException {
        JobConf conf = new JobConf();
        return new JobClient(new InetSocketAddress(conn.jobHostName,
                conn.jobPort), conf).getClusterStatus();
    }

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public String getConnName() {
        return connName;
    }

    public String getHDFSUrl() {
        return "hdfs://" + getHdfsHostName() + ":" + getHdfsPort();
    }

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(String securityMode) {
        this.securityMode = securityMode;
    }

    public String getHdfsPrincipal() {
        return hdfsPrincipal;
    }

    public void setHdfsPrincipal(String hdfsPrincipal) {
        this.hdfsPrincipal = hdfsPrincipal;
    }

    public String getHdfsKeyTab() {
        return hdfsKeyTab;
    }

    public void setHdfsKeyTab(String hdfsKeyTab) {
        this.hdfsKeyTab = hdfsKeyTab;
    }

    public String getMapredPrincipal() {
        return mapredPrincipal;
    }

    public void setMapredPrincipal(String mapredPrincipal) {
        this.mapredPrincipal = mapredPrincipal;
    }

    public String getMapredKeyTab() {
        return mapredKeyTab;
    }

    public void setMapredKeyTab(String mapredKeyTab) {
        this.mapredKeyTab = mapredKeyTab;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    private String hdfsHostName;

    public String getHdfsHostName() {
        return hdfsHostName;
    }

    public void setHdfsHostName(String hdfsHostName) {
        this.hdfsHostName = hdfsHostName;
    }

    public int getHdfsPort() {
        return hdfsPort;
    }

    public void setHdfsPort(int hdfsPort) {
        this.hdfsPort = hdfsPort;
    }

    public String getVersion() {
        return version.trim();
    }

    public void setVersion(String version) {
        this.version = version.trim();
    }

    public String getJobHostName() {
        return jobHostName;
    }

    public void setJobHostName(String jobHostName) {
        this.jobHostName = jobHostName;
    }

    public int getJobPort() {
        return jobPort;
    }

    public void setJobPort(int jobPort) {
        this.jobPort = jobPort;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public HadoopConnection() {

    }

    @Override
    public HadoopConnection clone() {

        return new HadoopConnection(connName, userName, groupName,
                hdfsHostName, hdfsPort, version, jobHostName, jobPort,
                securityMode, hdfsPrincipal, hdfsKeyTab, mapredPrincipal,
                mapredKeyTab);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((connName == null) ? 0 : connName.hashCode());
        result = prime * result
                + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result
                + ((hdfsHostName == null) ? 0 : hdfsHostName.hashCode());
        result = prime * result + hdfsPort;
        result = prime * result
                + ((jobHostName == null) ? 0 : jobHostName.hashCode());
        result = prime * result + jobPort;
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result
                + ((securityMode == null) ? 0 : securityMode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HadoopConnection other = (HadoopConnection) obj;
        if (connName == null) {
            if (other.connName != null)
                return false;
        } else if (!connName.equals(other.connName))
            return false;
        if (groupName == null) {
            if (other.groupName != null)
                return false;
        } else if (!groupName.equals(other.groupName))
            return false;
        if (hdfsHostName == null) {
            if (other.hdfsHostName != null)
                return false;
        } else if (!hdfsHostName.equals(other.hdfsHostName))
            return false;
        if (hdfsPort != other.hdfsPort)
            return false;
        if (jobHostName == null) {
            if (other.jobHostName != null)
                return false;
        } else if (!jobHostName.equals(other.jobHostName))
            return false;
        if (jobPort != other.jobPort)
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;

        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version)) {
            return false;
        }

        if (securityMode == null) {
            if (other.securityMode != null)
                return false;
        } else if (!securityMode.equals(other.securityMode)) {
            return false;
        }

        if (StringUtil.isEmpty(hdfsPrincipal) == true) {
            if (StringUtil.isEmpty(other.hdfsPrincipal) == false)
                return false;
        } else if (!hdfsPrincipal.equals(other.hdfsPrincipal)) {
            return false;
        }

        if (StringUtil.isEmpty(hdfsKeyTab) == true) {
            if (StringUtil.isEmpty(other.hdfsKeyTab) == false)
                return false;
        } else if (!hdfsKeyTab.equals(other.hdfsKeyTab)) {
            return false;
        }
        if (StringUtil.isEmpty(mapredPrincipal) == true) {
            if (StringUtil.isEmpty(other.mapredPrincipal) == false)
                return false;
        } else if (!mapredPrincipal.equals(other.mapredPrincipal)) {
            return false;
        }
        if (StringUtil.isEmpty(mapredKeyTab) == true) {
            if (StringUtil.isEmpty(other.mapredKeyTab) == false)
                return false;
        } else if (!mapredKeyTab.equals(other.mapredKeyTab)) {
            return false;
        }
        return true;

    }

    @Override
    public String toString() {
        return "HadoopConnection [connName=" + connName + ", homeDir="
                + ", userName=" + userName + ", hdfsHostName=" + hdfsHostName
                + ", hdfsPort=" + hdfsPort + ", version=" + version
                + ", jobHostName=" + jobHostName + ", jobPort=" + jobPort
                + ",securityMode=" + securityMode + " hdfsPrincipal="

                + hdfsPrincipal + ", hdfsKeyTab=" + hdfsKeyTab
                + ", mapredPrincipal=" + mapredPrincipal + ", mapredKeyTab="
                + mapredKeyTab + "]";

    }


}
