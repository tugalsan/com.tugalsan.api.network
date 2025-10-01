package com.tugalsan.api.network.server;

import module com.tugalsan.api.stream;
import module com.tugalsan.api.union;
import module com.tugalsan.api.function;
import module jcifs;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import java.net.*;
import java.util.*;

public class TS_NetworkSmbUtils {

    public static TGS_UnionExcuse<SmbFile> of(CharSequence smbLoc, CharSequence username, CharSequence password) {
        return TGS_FuncMTCUtils.call(() -> {
            var ctx = SingletonContext.getInstance();
            var auth = new NtlmPasswordAuthentication(ctx, null, username == null ? null : username.toString(), password == null ? null : password.toString());
            var ctxWithCredentials = SingletonContext.getInstance().withCredentials(auth);
            return TGS_UnionExcuse.of(new SmbFile(smbLoc.toString(), ctxWithCredentials));
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<byte[]> readBytes(SmbFile smbFile) {
        return readBytes_fromUrlConnection(smbFile);
    }

    private static TGS_UnionExcuse<byte[]> readBytes_fromUrlConnection(URLConnection urlConnection) {
        return TGS_FuncMTCUtils.call(() -> {
            return TGS_UnionExcuse.of(urlConnection.getInputStream().readAllBytes());
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static TGS_UnionExcuse<List<SmbFile>> list(SmbFile smbPath) {
        return TGS_FuncMTCUtils.call(() -> {
            return TGS_UnionExcuse.of(TGS_StreamUtils.toLst(Arrays.stream(smbPath.listFiles())));
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }
}
