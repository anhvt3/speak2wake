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
          DEFAULT: "#FFFFFF",
          dark: "#1C1721",
        },
        surface: {
          DEFAULT: "#F5F3F8",
          dark: "#2B2630",
        },
        violet: {
          DEFAULT: "#9E6DFB",
          light: "#8A70F8",
          pink: "#D28AED",
        },
        accent: {
          orange: "#F79F40",
          gold: "#FBF66E",
        },
        grey: {
          blueish: "#363843",
          medium: "#7F8187",
          dark: "#1C1721",
          surface: "#2B2630",
        },
        success: "#4ADE80",
        error: "#F87171",
        warning: "#FBBF24",
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
