# MyWallet Finance - Aplicación Híbrida de Finanzas Personales

Una aplicación moderna de finanzas personales para Android desarrollada en **Kotlin + Jetpack Compose (Material 3)**, que combina:
- **La solidez contable y lógica de presupuestos de Wallet by BudgetBakers**: transferencias de doble pata entre cuentas, saldos consolidados en tiempo real y motor de presupuestos con remanente acumulativo (**Rollover**).
- **La velocidad y simplicidad visual de Cashew / Money Manager**: registro rápido de transacciones en menos de 3 segundos mediante un teclado numérico integrado con calculadora y sin retraso de teclado del sistema.
- **Automatización Zero-Click**: Servicio nativo `PaymentNotificationListenerService` para capturar notificaciones de Google Wallet y entidades bancarias (BBVA, Santander, CaixaBank, Revolut, etc.) y registrar gastos automáticamente.

---

## 🏗️ Arquitectura del Proyecto

```
app/src/main/java/com/mywallet/finance/
├── MainActivity.kt                          # Actividad principal Edge-to-Edge con Compose
├── MyWalletApp.kt                           # Application class con inicialización de Room
├── data/
│   ├── AppDatabase.kt                       # Room Database con Callback de pre-población
│   ├── model/
│   │   ├── AccountEntity.kt                 # Cuentas bancarias, efectivo y ahorros
│   │   ├── CategoryEntity.kt                # Categorías jerárquicas con iconos y colores
│   │   ├── TransactionEntity.kt             # Gastos, ingresos y transferencias vinculadas
│   │   ├── BudgetEntity.kt                  # Presupuestos por periodo con soporte de Rollover
│   │   └── MerchantRuleEntity.kt            # Reglas de comercios para auto-categorización
│   └── dao/
│       ├── AccountDao.kt
│       ├── CategoryDao.kt
│       ├── TransactionDao.kt
│       ├── BudgetDao.kt
│       └── MerchantRuleDao.kt
├── domain/
│   ├── BalanceCalculator.kt                 # Motor contable de saldos y patrimonio neto
│   ├── BudgetRolloverEngine.kt              # Motor de cálculo de presupuestos y remanentes
│   └── DataBackupManager.kt                 # Exportador e importador en formatos JSON y CSV
├── service/
│   └── PaymentNotificationListenerService.kt# Listener de notificaciones y extractor Regex
└── ui/
    ├── theme/                               # Tema Material 3, tipografía y colores
    ├── navigation/
    │   └── AppNavigation.kt                 # Barra de navegación inferior reactiva
    ├── components/
    │   ├── CustomNumpad.kt                  # Teclado táctil embebido con calculadora (+, -, =)
    │   ├── PieChartCanvas.kt                # Gráfico circular Donut dibujado en Canvas
    │   ├── BarChartCanvas.kt                # Gráfico de barras comparativo Ingresos vs Gastos
    │   └── QuickTransactionModal.kt         # Modal de registro ultrarrápido (< 3s)
    └── screens/
        ├── HomeScreen.kt                    # Patrimonio neto, carrusel de cuentas y transacciones
        ├── BudgetsAndStatsScreen.kt         # Gráficos interactivos y control de presupuestos
        └── SettingsAndRulesScreen.kt        # Simulador de pagos, permisos y reglas
```

---

## ⚡ Características Principales

1. **Patrimonio Neto & Cuentas**:
   - Seguimiento multi-cuenta (Efectivo, Cuentas Corrientes, Ahorros).
   - Transferencias directas que impactan simultáneamente ambas cuentas (débito y crédito).

2. **Registro Ultrarrápido (< 3 segundos)**:
   - Al pulsar el botón `+`, se abre el modal con teclado numérico integrado.
   - Permite escribir directamente operaciones como `15 + 4.5 =` para calcular totales antes de guardar.

3. **Presupuestos Inteligentes con Rollover**:
   - Fórmula: $\text{Presupuesto} = \text{Límite} - \sum \text{Gastos} + \text{Sobrante mes anterior}$.
   - Notifica y resalta visualmente el estado del presupuesto y el porcentaje consumido.

4. **Detector de Pagos Automático (Zero-Click)**:
   - Captura notificaciones en segundo plano de Google Wallet y bancos españoles/internacionales.
   - Analiza el comercio (ej. "Mercadona", "Repsol", "Uber") y el importe en euros.
   - Asigna automáticamente la categoría y la cuenta según tus `MerchantRules`.
   - Incluye un **Simulador de Pagos** en la pantalla de Ajustes para probar y verificar el funcionamiento al instante sin esperar a un pago real.

5. **Privacidad y Offline-First**:
   - 100% de los datos se guardan en la base de datos local SQLite (Room).
   - Exportación e importación completa en un toque a archivos `.json` y `.csv`.

---

## 🚀 Compilación y Ejecución

Para compilar el proyecto en Android Studio o por línea de comandos:

```bash
# Compilar APK de depuración
./gradlew assembleDebug

# O ejecutar pruebas
./gradlew test
```
