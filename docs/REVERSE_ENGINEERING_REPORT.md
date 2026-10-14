# INFORME DE INGENIERÍA INVERSA: ANÁLISIS DE WALLET & CASHEW
**Fecha:** 14 de Octubre de 2026  
**Herramientas utilizadas:** `rea` (Reverse Engineer Anything v6.5.0, JADX Headless MCP 0.7.1, OpenJDK 21)  
**Objetivos analizados:**
- `wallet.apk` (Wallet by BudgetBakers - `com.droid4you.application.wallet`) - 33.8 MB
- `cashew.apk` (Cashew Expense Tracker - `com.budget.tracker_app`) - 40.5 MB

---

## 1. ARQUITECTURA Y ESQUEMA DE WALLET (BUDGETBAKERS)

A través de la inspección de clases con `rea inspect-android-class` y `rea search-android-classes`, se descompusieron las entidades fundamentales del paquete `com.budgetbakers.modules.data.model`:

### 1.1 Modelo `Account` (Cuentas)
- **Campos descubiertos:**
  - `name`: `String` (Nombre descriptivo, ej. "Cuenta Corriente", "Efectivo").
  - `accountType`: Enum `Account$Type` (`REGULAR`, `SAVINGS`, `DEBIT_CARD`, `CREDIT_CARD`, `LOAN`, `INSURANCE`, `INVESTMENT`).
  - `currencyId`: `String` (Código ISO 4217, ej. "EUR", "USD").
  - `initAmount`: `long` (Saldo inicial almacenado en céntimos/enteros para evitar imprecisiones de punto flotante).
  - `color`: `String` (Hexadecimal `#RRGGBB`).
  - `excludeFromStats`: `boolean` (Si es `true`, no suma al patrimonio neto total).
  - `mArchived`: `boolean` (Oculta cuentas inactivas sin borrar el historial).
  - `enabled`: `boolean`.

### 1.2 Modelo `Record` / `Transaction` (Movimientos y Transferencias)
- **Campos descubiertos:**
  - `mAmount`: `Amount` / `mRefAmount`: `long` (Importe de la transacción).
  - `mRecordDate`: `DateTime` (Marca de tiempo de ejecución).
  - `mPayee`: `String` (Nombre del comercio o beneficiario, ej. "Mercadona").
  - `categoryId`: `String` (Clave foránea hacia la categoría).
  - `note`: `String` (Detalles adicionales).
  - `mTransfer`: `boolean` (Bandera crítica: indica si la operación es una transferencia).
  - `mTransferAccountId`: `String` (ID de la cuenta vinculada).
  - `mTransferId`: `String` (UUID que enlaza ambas patas de la transferencia: el debito de origen y el crédito de destino).
  - `mSuggestedEnvelopeId`: `int` (Asignación automática a presupuesto/sobre).

### 1.3 Modelo `Category` (Categorías y Jerarquía)
- **Campos descubiertos:**
  - `name`: `String` (Nombre de categoría).
  - `color`: `String` (Color de representación).
  - `iconName`: `String` / `icon`: `int` (Identificador del glifo).
  - `parentId`: `String` (Soporte nativo para subcategorías jerárquicas).
  - `defaultType`: Enum `RecordType` (`EXPENSE` o `INCOME`).
  - `systemCategory`: `boolean` (Categoría protegida del sistema preinstalada).

### 1.4 Modelo `EnvelopeBudget` & `EnvelopeBudgetUnit` (Presupuestos & Rollover)
- **Estructura contable:**
  - `mShortBudgetPeriod`: `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `YEARLY`.
  - `mCurrentBudgetEnvelopeUnit`: Unidad activa que contiene `mPlannedIncome`, `mTotalAmountToSpend` y `mSavedMoney`.
  - `mBudgetHistory`: Lista de unidades de periodos anteriores.
- **Fórmula Matemática de Rollover (Acumulación):**
  $$\text{Presupuesto Disponible}_{t} = \text{Límite Base} - \sum \text{Gastos}_{t} + (\text{Presupuesto Disponible}_{t-1} \times \mathbb{I}_{\text{rollover}})$$
  Si el rollover está activo y el mes anterior sobraron 80€, se suman al límite del mes actual. Si hubo sobregasto (-30€), se descuentan del mes actual.

---

## 2. ARQUITECTURA Y UX DE CASHEW & MONEY MANAGER

A través del análisis de `cashew.apk` y sus tablas:

### 2.1 Modelo `AssociatedTitles` (Reglas de Comercios)
- **Campos descubiertos:**
  - `title`: `String` (Patrón de texto detectado, ej. "Mercadona", "Uber", "Amazon").
  - `categoryFk`: `String` (Categoría a la que se debe mapear automáticamente).
  - `isExactMatch`: `boolean` (Si debe coincidir palabra completa o basta contener la subcadena).

### 2.2 Patrones de Interacción Rápida (< 3 Segundos)
- **Teclado numérico embebido:** La entrada de importe no invoca el teclado del sistema operativo (que tarda 300-500ms en desplegarse y ocupa la mitad de la pantalla). En su lugar, utiliza un `CustomNumpad` integrado en la vista que incluye operaciones aritméticas básicas (`+`, `-`, `=`).
- **Selector de cuentas y categorías en carrusel táctil:** Categorías accesibles inmediatamente bajo el importe sin navegación a pantallas secundarias.

---

## 3. ESPECIFICACIÓN DE NUESTRA APLICACIÓN HÍBRIDA

Con base en estas evidencias, nuestra aplicación unifica:
1. **La solidez contable de Wallet**: Cuentas con saldo inicial, transferencias de doble pata (`accountId` y `toAccountId`), presupuestos con rollover y categorías jerárquicas.
2. **La velocidad visual de Cashew**: Teclado numérico en pantalla, Material 3, resumen de patrimonio neto e inserción en dos toques.
3. **Automatización Zero-Click**: Servicio `NotificationListenerService` que lee notificaciones bancarias y de Google Wallet, aplica reglas de comercio y registra gastos sin que el usuario tenga que abrir la app.
