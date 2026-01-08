# NeoForge 1.21.1 迁移说明

## 已完成的迁移工作

### 1. gradle.properties
- ✅ 更新 Minecraft 版本: 1.20.1 → 1.21.1
- ✅ 替换 Forge → NeoForge
  - forge_version → neoforge_version (21.1.77)
  - forge_version_range → neoforge_version_range ([21.1,))
- ✅ 更新 loader_version_range: [47,) → [4,)
- ✅ 更新 Parchment mappings: 2023.08.20-1.20.1 → 2024.07.28-1.21.1
- ✅ 更新模组版本: 2.1.3-forge+mc1.20.1 → 2.2.0-neoforge+mc1.21.1

### 2. settings.gradle
- ✅ 替换 MinecraftForge Maven → NeoForged Maven
  - URL: https://maven.minecraftforge.net/ → https://maven.neoforged.net/releases
- ✅ 更新 Gradle Toolchains: 0.7.0 → 0.8.0

### 3. build.gradle
- ✅ 替换插件: net.minecraftforge.gradle → net.neoforged.gradle.userdev (7.0.163)
- ✅ 更新 Java 版本: 17 → 21 (1.21.1 需要 Java 21)
- ✅ 更新 minecraft runs 配置
  - 使用 systemProperty 替代 property
  - 使用 programArguments.addAll 替代 args
  - 移除 Mixin 相关的 VM 参数（NeoForge 自动处理）
- ✅ 更新依赖项
  - minecraft "net.minecraftforge:forge:..." → implementation "net.neoforged:neoforge:..."
  - 注释掉 Embeddium（需要检查 1.21.1 兼容版本）
- ✅ 更新 processResources 任务
  - META-INF/mods.toml → META-INF/neoforge.mods.toml

### 4. gradle-wrapper.properties
- ✅ 更新 Gradle 版本: 8.8 → 8.10 (NeoForge 推荐版本)

### 5. META-INF/neoforge.mods.toml
- ✅ 创建新的 NeoForge 模组元数据文件
- ✅ 更新依赖项: forge → neoforge
- ✅ 使用 type = "required" 替代 mandatory = true
- ✅ 添加 [[mixins]] 配置节

### 6. pack.mcmeta
- ✅ 更新 pack_format: 15 → 34 (1.21.1 资源包格式)
- ✅ 添加 supported_formats 范围

## 需要手动完成的工作

### 1. 代码迁移
需要检查并更新以下 Java 代码：

#### SimpleBedrockModel.java
```java
// Forge: @Mod(SimpleBedrockModel.MOD_ID)
// NeoForge: 保持不变，但需要检查其他类
```

#### 事件处理
- Forge 事件总线 → NeoForge 事件总线
- 检查所有使用 `@SubscribeEvent` 的类
- 检查 `IEventBus` 相关代码

#### 网络处理
- 检查 NetworkHandler.java
- SimpleChannel → 可能需要更新为 NeoForge 的网络 API

#### Capability 系统
- NeoForge 1.21+ 使用新的数据附件系统（Data Attachments）
- 需要迁移所有 Capability 代码
- 检查: `FPGunAnimationCapability`, `FPGunAnimationCapabilityProvider`, `IFPGunAnimationCapability`, `ModCapability`

#### 注册系统
- 检查 `ExampleModRegister.java` 和 `RegistryHandler.java`
- DeferredRegister 在 NeoForge 中有变化

#### 客户端事件
- 检查 `ClientTicker.java` 和相关事件处理器

### 2. Mixin 配置
检查 `simplebedrockmodel.mixins.json` 文件：
- 确保 Mixin 目标类在 1.21.1 中仍然存在
- 可能需要更新一些方法签名和字段名

### 3. 资源文件
- ✅ pack.mcmeta 已更新
- 检查所有模型、动画、音效文件是否兼容 1.21.1

### 4. 依赖库
- MAE 库 (com.maydaymemory:mae:1.1.1) - 需要确认是否有 1.21.1 兼容版本
- Embeddium - 已注释，需要找到 1.21.1 兼容版本

### 5. 测试
- 运行 `./gradlew clean build` 检查编译错误
- 测试所有功能是否正常工作
- 测试示例模组（example 包）

## 下一步操作

1. **清理构建缓存**
   ```powershell
   .\gradlew clean
   ```

2. **刷新依赖**
   ```powershell
   .\gradlew --refresh-dependencies
   ```

3. **生成 IDE 配置**
   ```powershell
   .\gradlew genIntellijRuns
   ```

4. **尝试构建**
   ```powershell
   .\gradlew build
   ```

5. **处理编译错误**
   - 根据错误信息更新代码
   - 参考 NeoForge 1.21.1 文档进行 API 迁移

## 重要变更说明

### Java 21 要求
Minecraft 1.21.1 需要 Java 21。确保：
- IDE 配置使用 Java 21
- JAVA_HOME 环境变量指向 Java 21

### NeoForge vs Forge 主要差异
1. **包名变化**: net.minecraftforge → net.neoforged
2. **Capability → Data Attachments**: 完全重写的附加数据系统
3. **事件系统**: 部分事件可能有变化
4. **网络系统**: 更简化的 API
5. **模组总线**: 分离的模组事件总线和游戏事件总线

## 参考资源
- [NeoForge 官方文档](https://docs.neoforged.net/)
- [1.21 迁移指南](https://docs.neoforged.net/docs/1.21.x/gettingstarted/migration/)
- [NeoForge Discord](https://discord.neoforged.net/)

## 注意事项
- 旧的 `META-INF/mods.toml` 文件可以删除（已被 `neoforge.mods.toml` 替代）
- Example 包在打包时会被排除
- 确保所有团队成员都更新到 Java 21

