package engine.assets;

import engine.assets.MtlParser;
import engine.assets.ObjMeshBuilder;
import engine.assets.ObjParseException;
import engine.math.Vec3;
import engine.render.Material;
import engine.render.Mesh;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ObjParser {
    private ObjParser() {
    }

    public static List<Mesh> parseFile(Path objFile) throws IOException, ObjParseException {
        Path base = objFile.toAbsolutePath().getParent();
        String text = Files.readString(objFile, StandardCharsets.UTF_8);
        return ObjParser.parseText(text, objFile, base);
    }

    public static List<Mesh> parseText(String text, Path src, Path mtlDir) throws ObjParseException {
        ArrayList<Vec3> pos = new ArrayList<Vec3>();
        ArrayList<float[]> uvs = new ArrayList<float[]>();
        ArrayList<Vec3> nrm = new ArrayList<Vec3>();
        HashMap<String, Material> mats = new HashMap<String, Material>();
        String active = null;
        ArrayList<int[]> fV = new ArrayList<int[]>();
        ArrayList<int[]> fT = new ArrayList<int[]>();
        ArrayList<int[]> fN = new ArrayList<int[]>();
        ArrayList<String> fM = new ArrayList<String>();
        int lineNo = 0;
        try (BufferedReader br = new BufferedReader(new StringReader(text));){
            String line;
            while ((line = br.readLine()) != null) {
                ++lineNo;
                if ((line = ObjParser.noComment(line).trim()).isEmpty()) continue;
                String[] tok = line.split("\\s+");
                switch (tok[0]) {
                    case "v": {
                        ObjParser.need(tok, 4, src, lineNo, "v expects x y z");
                        pos.add(new Vec3(ObjParser.num(tok[1], src, lineNo), ObjParser.num(tok[2], src, lineNo), ObjParser.num(tok[3], src, lineNo)));
                        break;
                    }
                    case "vt": {
                        ObjParser.need(tok, 3, src, lineNo, "vt expects u v");
                        uvs.add(new float[]{ObjParser.num(tok[1], src, lineNo), ObjParser.num(tok[2], src, lineNo)});
                        break;
                    }
                    case "vn": {
                        ObjParser.need(tok, 4, src, lineNo, "vn expects x y z");
                        nrm.add(new Vec3(ObjParser.num(tok[1], src, lineNo), ObjParser.num(tok[2], src, lineNo), ObjParser.num(tok[3], src, lineNo)));
                        break;
                    }
                    case "f": {
                        ObjParser.need(tok, 4, src, lineNo, "f expects >= 3 vertices");
                        int n = tok.length - 1;
                        int[] v = new int[n];
                        int[] t = new int[n];
                        int[] nn = new int[n];
                        for (int i = 0; i < n; ++i) {
                            String[] p = tok[i + 1].split("/", -1);
                            v[i] = ObjParser.idx(p[0], pos.size());
                            t[i] = p.length > 1 && !p[1].isEmpty() ? ObjParser.idx(p[1], uvs.size()) : -1;
                            nn[i] = p.length > 2 && !p[2].isEmpty() ? ObjParser.idx(p[2], nrm.size()) : -1;
                        }
                        fV.add(v);
                        fT.add(t);
                        fN.add(nn);
                        fM.add(active);
                        break;
                    }
                    case "mtllib": {
                        ObjParser.need(tok, 2, src, lineNo, "mtllib without file");
                        if (mtlDir == null) break;
                        Path m = mtlDir.resolve(tok[1]).normalize();
                        try {
                            mats.putAll(MtlParser.parse(m));
                            break;
                        }
                        catch (IOException e) {
                            throw new ObjParseException(src, lineNo, "unreadable mtllib: " + m, e);
                        }
                    }
                    case "usemtl": {
                        active = tok.length > 1 ? tok[1] : null;
                        break;
                    }
                }
            }
        }
        catch (IOException e) {
            throw new ObjParseException(src, 0, "lecture impossible", e);
        }
        if (pos.isEmpty()) {
            throw new ObjParseException(src, 0, "aucun sommet v");
        }
        if (fV.isEmpty()) {
            throw new ObjParseException(src, 0, "aucune face f");
        }
        return ObjMeshBuilder.build(pos, uvs, nrm, fV, fT, fN, fM, mats);
    }

    static int idx(String raw, int count) throws ObjParseException {
        int i;
        int r;
        try {
            r = Integer.parseInt(raw);
        }
        catch (NumberFormatException e) {
            throw new ObjParseException(null, 0, "invalid index: " + raw, e);
        }
        int n = i = r > 0 ? r - 1 : count + r;
        if (i < 0 || i >= count) {
            throw new ObjParseException(null, 0, "index out of bounds: " + raw);
        }
        return i;
    }

    private static String noComment(String l) {
        int i = l.indexOf(35);
        return i < 0 ? l : l.substring(0, i);
    }

    private static void need(String[] t, int n, Path s, int ln, String m) throws ObjParseException {
        if (t.length < n) {
            throw new ObjParseException(s, ln, m);
        }
    }

    private static float num(String s, Path f, int ln) throws ObjParseException {
        try {
            return Float.parseFloat(s);
        }
        catch (NumberFormatException e) {
            throw new ObjParseException(f, ln, "invalid number: " + s, e);
        }
    }
}
