package me.pajic.rearm.mixin.base.jsonpatcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import io.github.mattidragon.jsonpatcher.lang.runtime.EvaluationContext;
import io.github.mattidragon.jsonpatcher.misc.GsonConverter;
import io.github.mattidragon.jsonpatcher.patch.Patcher;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.FileNotFoundException;
import java.io.FileReader;

@Mixin(value = Patcher.class, remap = false)
public class PatcherMixin {

    @Shadow @Final private static Gson GSON;

    @ModifyReceiver(
            method = "buildContext",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/mattidragon/jsonpatcher/lang/runtime/EvaluationContext$Builder;build()Lio/github/mattidragon/jsonpatcher/lang/runtime/EvaluationContext;"
            )
    )
    private static EvaluationContext.Builder addModConfigAsGlobalVariable(EvaluationContext.Builder instance) throws FileNotFoundException {
        return instance.variable(
                "rearm_config",
                GsonConverter.fromGson(GSON.fromJson(
                        new FileReader(FabricLoader.getInstance().getConfigDir().resolve("rearm.json5").toFile()),
                        JsonElement.class)
                )
        );
    }

    @ModifyArg(
            method = "runPatch",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;get(JLjava/util/concurrent/TimeUnit;)Ljava/lang/Object;"
            ),
            index = 0
    )
    private static long modifyPatchTimeout(long original) {
        if (original < 1000) {
            return 1000;
        }
        return original;
    }
}
