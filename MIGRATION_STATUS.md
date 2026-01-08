# NeoForge 1.21.1 Migration Status

## ✅ Completed Migrations

### Core Library Files (`com.github.mcmodderanchor.simplebedrockmodel.v1`)

1. **ClientSetupEvent.java** ✅
   - Changed `net.minecraftforge.eventbus.api` → `net.neoforged.bus.api`
   - Changed `net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.EventBusSubscriber`
   - Changed `@Mod.EventBusSubscriber` → `@EventBusSubscriber`

2. **SodiumCompat.java** ✅
   - Changed `net.minecraftforge.fml.ModList` → `net.neoforged.fml.ModList`

3. **Event Classes** ✅
   - **RenderItemInHandBobEvent.java**: Changed to use `net.neoforged.bus.api.Event` and `ICancellableEvent`
   - **RenderLevelBobEvent.java**: Changed to use `net.neoforged.bus.api.Event` and `ICancellableEvent`
   - **SwapItemWithOffHand.java**: Changed to use `net.neoforged.bus.api.Event`
   - **RegisterBedrockAnimationEvent.java**: Updated imports to NeoForge API
   - **RegisterBedrockAnimationReloadListenerEvent.java**: Updated imports to NeoForge API
   - **RegisterBedrockModelEvent.java**: Updated imports to NeoForge API

4. **FirstPersonRenderHandler.java** ✅
   - Updated all event imports to NeoForge
   - Changed `TickEvent.ClientTickEvent` → `PlayerTickEvent.Post`
   - Changed `TickEvent.RenderTickEvent` → `RenderFrameEvent.Pre`
   - Changed `@Mod.EventBusSubscriber` → `@EventBusSubscriber`
   - Updated to use `net.neoforged.neoforge.client.event.*`

5. **GameRendererMixin.java** ✅
   - Changed `MinecraftForge.EVENT_BUS` → `NeoForge.EVENT_BUS`
   - Updated event posting to use `.isCanceled()` method

6. **RawResourceLoaders.java** ✅
   - Changed `net.minecraftforge.fml.loading.FMLLoader` → `net.neoforged.fml.loading.FMLLoader`
   - Changed `net.neoforged.neoforge.api.distmarker` → `net.neoforged.api.distmarker`

7. **ReloadListenersRegister.java** ✅
   - Updated all imports to NeoForge API
   - Changed `Mod.EventBusSubscriber.Bus.FORGE` → `EventBusSubscriber.Bus.GAME`
   - Updated event imports

## ⚠️ Temporarily Stubbed (Needs Proper Implementation)

### Network System (CRITICAL - Needs Complete Rewrite)
NeoForge 1.21.1 has a completely new networking API. The old `SimpleChannel` system no longer exists.

**Files needing rewrite:**
1. **NetworkHandler.java** ⚠️
   - Old Forge `SimpleChannel` API is removed
   - Need to implement new NeoForge network registration
   - See: https://docs.neoforged.net/docs/networking/

2. **ServerMessageSwapItem.java** ⚠️
   - Network packet handling completely changed
   - Context supplier pattern is different
   - Needs rewrite with new payload system

**Migration Plan:**
- Use `SimpleChannel` replacement in NeoForge
- Implement `CustomPacketPayload` interface
- Update packet registration
- Fix context handling for client/server

### Sodium Compatibility (OPTIONAL - Can be Fixed Later)
Sodium API has changed significantly in 1.21.1 versions.

**Files temporarily disabled:**
1. **ISodiumVertexWriter.java** ⚠️
   - `ModelVertex` API changed
   - Commented out for now

2. **SodiumBedrockCubeBox.java** ⚠️
   - `VertexConsumerUtils` location changed
   - Falls back to vanilla rendering

3. **SodiumBedrockCubePerFace.java** ⚠️
   - Same as above
   - Falls back to vanilla rendering

**Migration Plan:**
- Wait for stable Sodium 1.21.1 release
- Update to new Sodium API when available
- Re-enable optimized rendering paths

## ❌ Not Yet Migrated (Example Package)

### Example Package Files
The example package demonstrates library usage but needs migration:

**Event handling files:**
- `example/event/Ticker.java` - Uses old TickEvent API
- `example/client/event/RegistryHandler.java` - Uses old event bus
- `example/resource/KnownResources.java` - Uses old event system
- `example/animation/TestBlockAnimationContext.java` - Uses old annotations

**Capability System (MAJOR CHANGE REQUIRED):**
NeoForge 1.21+ replaced Capabilities with Data Attachments system.

Files needing complete rewrite:
- `example/capability/ModCapability.java`
- `example/capability/IFPGunAnimationCapability.java`
- `example/capability/FPGunAnimationCapabilityProvider.java`

**Migration Plan:**
- Convert Capabilities to Data Attachments
- See: https://docs.neoforged.net/docs/datastorage/attachments/

**Client rendering:**
- `example/client/event/ClientTicker.java` - Uses old TickEvent
- Probably using deprecated IClientItemExtensions

## 🔧 Build Configuration

### Already Updated:
- ✅ build.gradle - Using net.neoforged.moddev plugin
- ✅ gradle.properties - Updated versions
- ✅ settings.gradle - NeoForge maven repository
- ✅ META-INF/neoforge.mods.toml - New format

## 📝 Next Steps (Priority Order)

### HIGH PRIORITY:
1. **Fix Network System**
   - Implement new NeoForge networking API
   - Rewrite NetworkHandler with SimpleChannel replacement
   - Update ServerMessageSwapItem packet handling
   - Test multiplayer functionality

### MEDIUM PRIORITY:
2. **Migrate Example Package**
   - Update event subscriptions
   - Convert Capabilities to Data Attachments
   - Fix tick event handlers
   - Update client rendering code

3. **Update Client Item Extensions**
   - Check for deprecation warnings
   - Update to new NeoForge client item API if needed

### LOW PRIORITY:
4. **Re-enable Sodium Optimizations**
   - Wait for stable Sodium 1.21.1
   - Update vertex writing code
   - Re-enable optimized render paths

5. **Testing**
   - Test all bedrock model loading
   - Test animations
   - Test first-person rendering
   - Test multiplayer (after network fix)
   - Test with Sodium/Embeddium

## 🔍 Known Issues

1. **Network functionality is disabled** - Offhand swapping won't work in multiplayer
2. **Sodium optimizations disabled** - May have slightly lower performance with Sodium
3. **Example code not functional** - Capability examples need complete rewrite
4. **Some deprecation warnings expected** - IClientItemExtensions may be deprecated

## 📚 Resources

- [NeoForge Official Docs](https://docs.neoforged.net/)
- [1.21.x Migration Guide](https://docs.neoforged.net/docs/1.21.x/gettingstarted/migration/)
- [Networking Documentation](https://docs.neoforged.net/docs/networking/)
- [Data Attachments Guide](https://docs.neoforged.net/docs/datastorage/attachments/)
- [NeoForge Discord](https://discord.neoforged.net/)

## ⚙️ Testing Commands

```powershell
# Clean build
.\gradlew clean

# Compile (will show errors if any)
.\gradlew compileJava

# Full build
.\gradlew build

# Run client
.\gradlew runClient

# Run server  
.\gradlew runServer
```

## 📅 Last Updated
2026-01-08 - Initial migration pass completed, core library functional except networking

