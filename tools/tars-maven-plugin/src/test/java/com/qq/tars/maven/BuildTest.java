package com.qq.tars.maven;

import com.qq.tars.maven.gensrc.Tars2JavaConfig;
import com.qq.tars.maven.gensrc.Tars2JavaMojo;
import com.qq.tars.maven.parse.TarsLexer;
import org.antlr.runtime.DFA;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BuildTest {

    @Test
    public void test() throws MojoFailureException, MojoExecutionException {

        String[] args = getBuildArgs();

        Tars2JavaMojo mojo = getTars2JavaMojo(args);
        mojo.execute();
    }

    private Tars2JavaMojo getTars2JavaMojo(String[] args) {
        Tars2JavaConfig config = new Tars2JavaConfig();
        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                String[] kv = arg.split("=");
                if (kv.length == 2) {
                    if ("--servant".equals(kv[0])) {
                        config.setServant("true".equalsIgnoreCase(kv[1]));
                    }
                    else if ("--charset".equals(kv[0])) {
                        Charset charset = Charset.forName(kv[1]);
                        config.setCharset(charset.displayName());
                        config.setTafFileCharset(charset.displayName());
                    }
                    else if ("--package".equals(kv[0])) {
                        String packageName = kv[1];
                        config.setPackagePrefixName(packageName.endsWith(".") ? packageName : packageName + ".");
                    }
                    else if ("--output".equals(kv[0])) {
                        config.setSrcPath(kv[1]);
                    }
                    else if ("--input".equals(kv[0])) {
                        config.setTafFiles(kv[1].trim().split(","));
                    }
                    else if ("--tup".equals(kv[0])) {
                        config.setTup("true".equalsIgnoreCase(kv[1]));
                    } else if("--tarsFileCharset".equals(kv[0])) {
                        config.setCharset(kv[1]);
                    }
                }
            }
        }
        Tars2JavaMojo mojo = new Tars2JavaMojo();
        mojo.setTars2JavaConfig(config);
        return mojo;
    }

    public String[] getBuildArgs() {
        List<String> params = new ArrayList<>();

        params.add("--charset=UTF-8");
        params.add("--package=com.qq.tars.maven.plugin");
        params.add("--input=" + BuildTest.class.getClassLoader().getResource("StructCacheVarName.tars").getFile());
        params.add("--output=/var/tmp");
        params.add("--tup=true");
        params.add("--type=all");
        params.add("--simplifyJceSpec=true");
        params.add("--usePrimitiveWrapper=true");
        params.add("--allowPrimitiveNull=true");
        params.add("--tarsFileCharset=UTF-8");

        String[] arrs = new String[params.size()];
        params.toArray(arrs);
        return arrs;
    }

    @Test
    public void test2() {
        short[] DFA18_eot = DFA.unpackEncodedString(TarsLexer.DFA18_eotS);
    }
}
