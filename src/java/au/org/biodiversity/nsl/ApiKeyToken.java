package au.org.biodiversity.nsl;

import org.apache.shiro.authc.HostAuthenticationToken;

/**
 * User: pmcneil
 * Date: 3/06/15
 */
public class ApiKeyToken implements HostAuthenticationToken {

    private String key;
    private char[] secret;
    private String host;

    public ApiKeyToken(String key) {
        this.key = key;
    }

    public ApiKeyToken(String key, String secret) {
        this(key, (char[]) (secret != null ? secret.toCharArray() : null));
    }

    public ApiKeyToken(String key, char[] secret) {
        this.key = key;
        this.secret = secret;
    }

    public ApiKeyToken(String key, String secret, String host) {
        this(key, (char[]) (secret != null ? secret.toCharArray() : null), host);
    }

    public ApiKeyToken(String key, char[] secret, String host) {
        this.key = key;
        this.secret = secret;
        this.host = host;
    }

    public String getKey() {
        return key;
    }

    public char[] getSecret() {
        return secret;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Object getPrincipal() {
        return key;
    }

    @Override
    public Object getCredentials() {
        return secret;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append(" - ");
        sb.append(this.key);
        if (this.host != null) {
            sb.append(" (").append(this.host).append(")");
        }

        return sb.toString();
    }
}
