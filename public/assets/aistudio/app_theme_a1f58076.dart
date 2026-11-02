// Smart Mess Manager v2 - theme matched to the old app's look.
// Colors are estimated from your screenshots. If you have the exact hex
// values from the old source code, replace them in AppColors only.
// Needs: google_fonts in pubspec.yaml

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppColors {
  // Brand teal
  static const primary = Color(0xFF009688);
  static const primaryDark = Color(0xFF00695C);
  static const deepTeal = Color(0xFF004D40); // dimmed headers, dark titles
  static const tealTint = Color(0xFFE0F2F1); // balance card, chips, icon bg

  // Page + surfaces
  static const background = Color(0xFFF4F6FA);
  static const card = Colors.white;
  static const border = Color(0xFF757575);
  static const textDark = Color(0xFF1A1A1A);
  static const textMuted = Color(0xFF9E9E9E);

  // Status
  static const positive = Color(0xFF43A047);
  static const negative = Color(0xFFEF5350);
  static const warning = Color(0xFFF57C00);
  static const warningTint = Color(0xFFFFF3E0);

  // Section header colors (each area has its own app bar color)
  static const charts = Color(0xFF1D5BD8); // Charts / reports
  static const mealSheet = Color(0xFF01579B); // Meal sheet
  static const roles = Color(0xFF9C27B0); // Manage roles
  static const danger = Color(0xFFFF5252); // Start new month / delete
  static const indigo = Color(0xFF3F51B5); // Join mess button

  // Meal slots
  static const breakfast = Color(0xFFFF9800);
  static const lunch = Color(0xFF4CAF50);
  static const dinner = Color(0xFF2196F3);

  // Meal rate card
  static const rateTint = Color(0xFFE3F2FD);
  static const rateText = Color(0xFF1565C0);
}

class AppRadius {
  static const card = 24.0;
  static const input = 12.0;
  static const button = 28.0;
  static const sheet = 32.0;
}

class AppTheme {
  static ThemeData light() {
    final base = ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.primary,
        primary: AppColors.primary,
        surface: AppColors.card,
        error: AppColors.negative,
      ),
      scaffoldBackgroundColor: AppColors.background,
    );

    // Poppins for English, Hind Siliguri as Bangla fallback
    final text = GoogleFonts.poppinsTextTheme(base.textTheme).apply(
      bodyColor: AppColors.textDark,
      displayColor: AppColors.textDark,
      fontFamilyFallback: [GoogleFonts.hindSiliguri().fontFamily!],
    );

    return base.copyWith(
      textTheme: text,
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: text.titleLarge?.copyWith(
            color: Colors.white, fontWeight: FontWeight.w700, fontSize: 22),
      ),
      cardTheme: CardThemeData(
        color: AppColors.card,
        elevation: 2,
        shadowColor: Colors.black12,
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppRadius.card)),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(56),
          elevation: 6,
          shadowColor: AppColors.primary.withOpacity(0.4),
          textStyle: text.titleMedium
              ?.copyWith(fontWeight: FontWeight.w700, letterSpacing: 0.5),
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppRadius.button)),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          minimumSize: const Size.fromHeight(56),
          side: const BorderSide(color: AppColors.primary, width: 1.5),
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppRadius.button)),
        ),
      ),
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        elevation: 6,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.transparent,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 18),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.input),
          borderSide: const BorderSide(color: AppColors.border, width: 1.2),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.input),
          borderSide: const BorderSide(color: AppColors.border, width: 1.2),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.input),
          borderSide: const BorderSide(color: AppColors.primary, width: 2),
        ),
        hintStyle: const TextStyle(color: AppColors.textMuted),
        labelStyle: const TextStyle(color: AppColors.primary),
      ),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: Colors.white,
        showDragHandle: true,
        shape: RoundedRectangleBorder(
          borderRadius:
              BorderRadius.vertical(top: Radius.circular(AppRadius.sheet)),
        ),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: const Color(0xFFF5FAF9),
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(28)),
      ),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.all(AppColors.primary),
        trackColor: WidgetStateProperty.all(
            AppColors.primary.withOpacity(0.5)),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.tealTint,
        labelStyle: const TextStyle(color: AppColors.primaryDark),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
    );
  }

  // Dark mode keeps the same teal identity
  static ThemeData dark() {
    final l = light();
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.primary, brightness: Brightness.dark),
      scaffoldBackgroundColor: const Color(0xFF10181A),
      textTheme: l.textTheme.apply(
          bodyColor: Colors.white, displayColor: Colors.white),
      appBarTheme: l.appBarTheme.copyWith(backgroundColor: AppColors.deepTeal),
      elevatedButtonTheme: l.elevatedButtonTheme,
      inputDecorationTheme: l.inputDecorationTheme,
    );
  }
}

// ---------- Reusable look-alike widgets ----------

/// Rounded white section card with soft shadow (used on every screen)
class AppCard extends StatelessWidget {
  final Widget child;
  final EdgeInsets padding;
  const AppCard(
      {super.key, required this.child, this.padding = const EdgeInsets.all(20)});

  @override
  Widget build(BuildContext context) => Container(
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        padding: padding,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(AppRadius.card),
          boxShadow: const [
            BoxShadow(color: Color(0x14000000), blurRadius: 16, offset: Offset(0, 6))
          ],
        ),
        child: child,
      );
}

/// Highlight row: Mess Balance (teal) or Meal Rate (blue)
class StatTile extends StatelessWidget {
  final String label, value;
  final Color bg, fg;
  final IconData icon;
  const StatTile.balance(
      {super.key, required this.label, required this.value})
      : bg = AppColors.tealTint,
        fg = AppColors.primaryDark,
        icon = Icons.account_balance_wallet_outlined;
  const StatTile.rate({super.key, required this.label, required this.value})
      : bg = AppColors.rateTint,
        fg = AppColors.rateText,
        icon = Icons.calculate_outlined;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 22),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: fg.withOpacity(0.25)),
        ),
        child: Row(children: [
          Icon(icon, color: fg),
          const SizedBox(width: 12),
          Text(label,
              style: TextStyle(
                  color: fg, fontSize: 20, fontWeight: FontWeight.w700)),
          const Spacer(),
          Text(value,
              style: TextStyle(
                  color: fg, fontSize: 26, fontWeight: FontWeight.w800)),
        ]),
      );
}

/// Breakfast / Lunch / Dinner count box on the home screen
class MealSlotBox extends StatelessWidget {
  final String label;
  final num count;
  final Color color;
  const MealSlotBox(
      {super.key, required this.label, required this.count, required this.color});

  @override
  Widget build(BuildContext context) => Expanded(
        child: Container(
          margin: const EdgeInsets.symmetric(horizontal: 4),
          padding: const EdgeInsets.symmetric(vertical: 16),
          decoration: BoxDecoration(
            color: color.withOpacity(0.07),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: color.withOpacity(0.5), width: 1.4),
          ),
          child: Column(children: [
            Text(label,
                style: TextStyle(color: color, fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            Text('$count',
                style:
                    const TextStyle(fontSize: 30, fontWeight: FontWeight.w800)),
          ]),
        ),
      );
}

/// Floating pill bottom navigation: Charts, History, Home, Summary, Profile.
/// Selected item is a raised white circle with a glowing gradient ring.
class PillNavBar extends StatelessWidget {
  final int index;
  final ValueChanged<int> onTap;
  const PillNavBar({super.key, required this.index, required this.onTap});

  static const _items = [
    (Icons.bar_chart_rounded, 'Charts'),
    (Icons.history_rounded, 'History'),
    (Icons.home_rounded, 'Home'),
    (Icons.assignment_outlined, 'Summary'),
    (Icons.person_outline_rounded, 'Profile'),
  ];

  @override
  Widget build(BuildContext context) => Container(
        margin: const EdgeInsets.fromLTRB(16, 0, 16, 16),
        height: 76,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(38),
          gradient: const LinearGradient(
              colors: [Color(0xFFDDF3F5), Color(0xFFF6EEF8)]),
          boxShadow: const [
            BoxShadow(color: Color(0x22000000), blurRadius: 20, offset: Offset(0, 8))
          ],
        ),
        child: Row(
          children: List.generate(_items.length, (i) {
            final selected = i == index;
            final (icon, label) = _items[i];
            return Expanded(
              child: InkWell(
                borderRadius: BorderRadius.circular(38),
                onTap: () => onTap(i),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 250),
                      transform: Matrix4.translationValues(0, selected ? -12 : 0, 0),
                      padding: EdgeInsets.all(selected ? 14 : 0),
                      decoration: selected
                          ? BoxDecoration(
                              color: Colors.white,
                              shape: BoxShape.circle,
                              border: Border.all(
                                  color: const Color(0xFFB39DDB), width: 2),
                              boxShadow: const [
                                BoxShadow(
                                    color: Color(0x4400BCD4), blurRadius: 14)
                              ],
                            )
                          : null,
                      child: Icon(icon,
                          size: 28,
                          color: selected
                              ? AppColors.indigo
                              : const Color(0xFF78909C)),
                    ),
                    Text(label,
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: selected
                              ? AppColors.indigo
                              : const Color(0xFF78909C),
                        )),
                  ],
                ),
              ),
            );
          }),
        ),
      );
}
