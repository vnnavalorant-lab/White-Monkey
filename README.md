# White Monkey (Fabric 26.3)

Mod com o **White Monkey** (modelo do Blockbench), animações **idle / walk / eating**,
item aparecendo no bone **`item_eat`** ao comer, e **equipar no ombro**.

## Como usar no jogo
- `/summon white_monkey:white_monkey` ou o **Ovo de Geração** (aba Spawn Eggs).
- **Botão direito com comida** no macaco → ele come (animação `eating`, o item aparece no bone `item_eat`, ele se cura).
- **Shift + botão direito no macaco** → ele sobe no seu **ombro direito**.
- **Shift + botão direito em um bloco** (pode ser com a mão vazia) → coloca o macaco no chão.
- **Shift + botão direito no ar com qualquer item na mão** → o macaco desce do ombro.
- **Botão direito com um capacete** → o macaco veste (renderizado como armadura de verdade). Mão vazia → tira.
- Ao sair do mundo, o macaco desce do ombro automaticamente (não some).

## Compilar
1. **Java 25**. Gradle 9.6 (ou copie `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar`
   do template oficial em https://fabricmc.net/develop/template/ — só falta o `.jar` do wrapper).
2. `./gradlew runClient` para testar, `./gradlew build` para gerar o `.jar` em `build/libs/`.

Minecraft 26.3 (sem ofuscação, nomes oficiais da Mojang). Loom 1.17 (`net.fabricmc.fabric-loom`), Fabric Loader 0.19.5, Fabric API 0.161.0+26.3.

## Editar o modelo/animações no Blockbench
Salve o projeto em `tools/white_monkey.bbmodel` e rode:

    python3 tools/bbmodel_to_java.py tools/white_monkey.bbmodel --verify

Isso regenera `MonkeyModelData.java`, `MonkeyAnimations.java` e a textura. Os nomes dos grupos
(`head`, `left_arm`, `item_eat`...) devem ser mantidos. A animação de `eating` deve animar a
**escala** do grupo `item_eat` (0 → 1) para mostrar/esconder o item.

## Ajustes rápidos
- Posição/tamanho no ombro: constantes `SHOULDER_*` em `MonkeyRenderer.java`.
- Tamanho do item comido: `ITEM_SCALE` em `MonkeyEatItemLayer.java`.
- Duração de comer: `EAT_DURATION` em `MonkeyEntity.java`.
