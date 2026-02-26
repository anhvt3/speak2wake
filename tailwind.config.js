/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,jsx,ts,tsx}",
    "./components/**/*.{js,jsx,ts,tsx}",
  ],
  presets: [require("nativewind/preset")],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        background: {
          DEFAULT: "#FFF8F0",
          dark: "#141018",
        },
        surface: {
          DEFAULT: "#FFF1E6",
          dark: "#1E1924",
        },
        primary: {
          DEFAULT: "#FF914D",
          light: "#FFB380",
          dark: "#E8732A",
        },
        accent: {
          DEFAULT: "#FFBE5C",
          gold: "#FFD93D",
        },
        // Legacy aliases (components still use text-violet etc)
        violet: {
          DEFAULT: "#FF914D",
          light: "#FFB380",
          pink: "#FFBE5C",
        },
        grey: {
          blueish: "#363843",
          medium: "#7A6B60",
          dark: "#141018",
          surface: "#1E1924",
        },
        success: "#4ADE80",
        error: "#FF6B6B",
        warning: "#FFD93D",
        gradient: {
          start: "#FF6B35",
          mid: "#FF914D",
          end: "#FFBE5C",
        },
      },
      fontFamily: {
        "jost-regular": ["Jost_400Regular"],
        "jost-medium": ["Jost_500Medium"],
        "jost-semibold": ["Jost_600SemiBold"],
      },
      borderRadius: {
        card: "28px",
        button: "38px",
        pill: "24px",
      },
    },
  },
  plugins: [],
};
