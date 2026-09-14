package engine.assets;

import engine.assets.ObjParseException;
import engine.math.Vec3;
import engine.render.Material;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class MtlParser {
    private MtlParser() {
    }

    public static Map<String, Material> parse(Path mtlFile) throws IOException, ObjParseException {
        HashMap<String, Material> out = new HashMap<String, Material>();
        if (mtlFile == null || !Files.exists(mtlFile, new LinkOption[0])) {
            return out;
        }
        String current = null;
        float r = 0.8f;
        float g = 0.8f;
        float b = 0.8f;
        float ar = 0.2f;
        float ag = 0.2f;
        float ab = 0.2f;
        float sr = 0.5f;
        float sg = 0.5f;
        float sb = 0.5f;
        float shininess = 32.0f;
        String mapKd = null;
        float opacity = 1.0f;
        try (BufferedReader br = Files.newBufferedReader(mtlFile, StandardCharsets.UTF_8);){
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                ++lineNo;
                if ((line = MtlParser.stripComment(line).trim()).isEmpty()) continue;
                String[] tok = line.split("\\s+");
                switch (tok[0]) {
                    case "newmtl": {
                        if (current != null) {
                            out.put(current, MtlParser.build(current, r, g, b, ar, ag, ab, sr, sg, sb, shininess, mapKd, opacity));
                        }
                        if (tok.length < 2) {
                            throw new ObjParseException(mtlFile, lineNo, "newmtl without name");
                        }
                        current = tok[1];
                        b = 0.8f;
                        g = 0.8f;
                        r = 0.8f;
                        ab = 0.2f;
                        ag = 0.2f;
                        ar = 0.2f;
                        sb = 0.5f;
                        sg = 0.5f;
                        sr = 0.5f;
                        shininess = 32.0f;
                        mapKd = null;
                        opacity = 1.0f;
                        break;
                    }
                    case "Kd": {
                        if (tok.length < 4) {
                            throw new ObjParseException(mtlFile, lineNo, "Kd expects 3 components");
                        }
                        try {
                            r = Float.parseFloat(tok[1]);
                            g = Float.parseFloat(tok[2]);
                            b = Float.parseFloat(tok[3]);
                            break;
                        }
                        catch (NumberFormatException e) {
                            throw new ObjParseException(mtlFile, lineNo, "invalid Kd", e);
                        }
                    }
                    case "Ka": {
                        if (tok.length < 4) {
                            throw new ObjParseException(mtlFile, lineNo, "Ka expects 3 components");
                        }
                        try {
                            ar = Float.parseFloat(tok[1]);
                            ag = Float.parseFloat(tok[2]);
                            ab = Float.parseFloat(tok[3]);
                            break;
                        }
                        catch (NumberFormatException e) {
                            throw new ObjParseException(mtlFile, lineNo, "invalid Ka", e);
                        }
                    }
                    case "Ks": {
                        if (tok.length < 4) {
                            throw new ObjParseException(mtlFile, lineNo, "Ks expects 3 components");
                        }
                        try {
                            sr = Float.parseFloat(tok[1]);
                            sg = Float.parseFloat(tok[2]);
                            sb = Float.parseFloat(tok[3]);
                            break;
                        }
                        catch (NumberFormatException e) {
                            throw new ObjParseException(mtlFile, lineNo, "invalid Ks", e);
                        }
                    }
                    case "Ns": {
                        try {
                            shininess = Float.parseFloat(tok[1]);
                            break;
                        }
                        catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                            throw new ObjParseException(mtlFile, lineNo, "invalid Ns", e);
                        }
                    }
                    case "map_Kd": {
                        if (tok.length < 2) {
                            throw new ObjParseException(mtlFile, lineNo, "map_Kd without file");
                        }
                        mapKd = tok[tok.length - 1];
                        break;
                    }
                    case "d": {
                        try {
                            opacity = Float.parseFloat(tok[1]);
                            break;
                        }
                        catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                            throw new ObjParseException(mtlFile, lineNo, "invalid d (opacity)", e);
                        }
                    }
                }
            }
        }
        if (current != null) {
            out.put(current, MtlParser.build(current, r, g, b, ar, ag, ab, sr, sg, sb, shininess, mapKd, opacity));
        }
        return out;
    }

    private static Material build(String name, float r, float g, float b, float ar, float ag, float ab, float sr, float sg, float sb, float shininess, String mapKd, float opacity) {
        return new Material(name, MtlParser.pack(r, g, b), opacity, new Vec3(ar, ag, ab), new Vec3(sr, sg, sb), Math.max(0.0f, shininess), mapKd);
    }

    private static String stripComment(String line) {
        int i = line.indexOf(35);
        return i < 0 ? line : line.substring(0, i);
    }

    private static int pack(float r, float g, float b) {
        int ri = Math.max(0, Math.min(255, Math.round(r * 255.0f)));
        int gi = Math.max(0, Math.min(255, Math.round(g * 255.0f)));
        int bi = Math.max(0, Math.min(255, Math.round(b * 255.0f)));
        return ri << 16 | gi << 8 | bi;
    }
}
