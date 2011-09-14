package org.tmatesoft.svn.core.wc2;

import org.tmatesoft.svn.core.SVNException;

public class SvnChecksum {
    
    public enum Kind { sha1, md5 }
    
    private Kind kind;
    private String digest;
    
    public SvnChecksum() {
    }
    
    public SvnChecksum(Kind kind, String digest) {
        setKind(kind);
        setDigest(digest);
    }

    public Kind getKind() {
        return kind;
    }
    public String getDigest() {
        return digest;
    }
    public void setKind(Kind kind) {
        this.kind = kind;
    }
    public void setDigest(String digest) {
        this.digest = digest;
    }
    
    public String toString() {
        return '$' + (getKind() == Kind.md5 ? "md5 $" : "sha1$") + getDigest();
    }
    
    @Override
    public int hashCode() {
        int hashCode = 17;
        if (digest != null) {
            hashCode += 37*digest.hashCode();
        }
        if (kind != null) {
            hashCode += 37*kind.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != SvnChecksum.class) {
            return false;
        }        SvnChecksum other = (SvnChecksum) obj;
        
        boolean equals = true;
        if (other.digest == null) {
            equals = this.digest == null;
        } else {
            equals = other.digest.equals(this.digest);
        }
        if (equals) {
            equals = other.kind == this.kind;
        }
        return equals;
    }

    public static SvnChecksum fromString(String checksum) throws SVNException {
        if (checksum == null || checksum.length() < 7) {
            return null;
        }
        Kind kind = checksum.charAt(1) == 'm' ? Kind.md5: Kind.sha1;
        String digest = checksum.substring(6);
        return new SvnChecksum(kind, digest);
    }


}