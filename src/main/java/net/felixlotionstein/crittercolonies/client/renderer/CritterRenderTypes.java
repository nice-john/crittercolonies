package net.felixlotionstein.crittercolonies.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom {@link RenderType}s used by Critter Colonies.
 *
 * Subclasses {@code RenderType} to gain access to its protected {@code create()} factory.
 * The class is never instantiated; the dummy constructor is required by Java but
 * throws if anyone tries to construct it.
 */
public final class CritterRenderTypes extends RenderType {

    private CritterRenderTypes(String name, VertexFormat fmt, VertexFormat.Mode mode,
                                int bufSize, boolean affectsCrumbling, boolean sortOnUpload,
                                Runnable setup, Runnable clear) {
        super(name, fmt, mode, bufSize, affectsCrumbling, sortOnUpload, setup, clear);
        throw new IllegalStateException("Utility class");
    }

    /**
     * "Always-pass" depth test so the icon shows through walls and other geometry —
     * matches MineColonies' behaviour for the same icon. Without this, citizens
     * inside huts would have their alert icons hidden.
     */
    private static final RenderStateShard.DepthTestStateShard ALWAYS_DEPTH_TEST =
            new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS);

    /**
     * Memoized so we get one RenderType per texture path — the rendering pipeline
     * batches by RenderType identity, and creating a fresh instance per frame would
     * defeat that batching and leak vertex buffers.
     */
    private static final Map<ResourceLocation, RenderType> STATUS_ICON_CACHE =
            new ConcurrentHashMap<>();

    /**
     * Render type for billboarded citizen head icons (recruit, hungry, raided, etc.).
     * <ul>
     *   <li>POSITION_TEX vertex format — minimal, since we draw a flat textured quad</li>
     *   <li>Translucent transparency — icon PNGs have alpha edges</li>
     *   <li>Always-pass depth test — icon stays visible through buildings</li>
     * </ul>
     */
    public static RenderType statusIcon(ResourceLocation texture) {
        return STATUS_ICON_CACHE.computeIfAbsent(texture, tex ->
                RenderType.create(
                        "crittercolonies_status_icon",
                        DefaultVertexFormat.POSITION_TEX,
                        VertexFormat.Mode.QUADS,
                        1024,
                        false,
                        false,
                        RenderType.CompositeState.builder()
                                .setShaderState(RenderStateShard.POSITION_TEX_SHADER)
                                .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                                .setDepthTestState(ALWAYS_DEPTH_TEST)
                                .createCompositeState(false)));
    }
}
