/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        // Apple-inspired palette mapped onto existing primary-* usage so existing
        // templates keep working. primary-500/600/700 = Apple Blue family (accent).
        // primary-50/100/200 = light gray surfaces. primary-900/950 = near-black / black.
        primary: {
          50: '#f5f5f7',   // light gray section background
          100: '#ededf2',  // active/hover light surface
          200: '#e5e5ea',  // soft divider
          300: '#d2d2d7',  // muted border
          400: '#86868b',  // muted text / icon
          500: '#0071e3',  // Apple Blue — primary accent
          600: '#0066cc',  // link blue
          700: '#0077ed',  // lightly brighter for text links on light bg
          800: '#1d1d1f',  // near-black heading/button-dark
          900: '#1d1d1f',  // near-black
          950: '#000000',  // pure black
        },
        secondary: '#1d1d1f',
        apple: {
          black: '#000000',
          gray: '#f5f5f7',
          'near-black': '#1d1d1f',
          blue: '#0071e3',
          link: '#0066cc',
          'link-dark': '#2997ff',
          surface1: '#272729',
          surface2: '#262628',
          surface3: '#28282a',
          surface4: '#2a2a2d',
          surface5: '#242426',
          'btn-active': '#ededf2',
          'btn-light': '#fafafc',
        },
      },
      fontFamily: {
        // SF Pro renders natively on Apple platforms; Inter is a close web fallback.
        sans: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"SF Pro Text"',
          '"SF Pro Display"',
          'Inter',
          '"Helvetica Neue"',
          'Helvetica',
          'Arial',
          'sans-serif',
        ],
        display: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"SF Pro Display"',
          'Inter',
          '"Helvetica Neue"',
          'Helvetica',
          'Arial',
          'sans-serif',
        ],
        serif: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"SF Pro Display"',
          'Inter',
          '"Helvetica Neue"',
          'sans-serif',
        ],
      },
      fontSize: {
        'display': ['3.5rem', { lineHeight: '1.07', letterSpacing: '-0.28px', fontWeight: '600' }],
        'section': ['2.5rem', { lineHeight: '1.10', letterSpacing: '-0.5px', fontWeight: '600' }],
        'tile': ['1.75rem', { lineHeight: '1.14', letterSpacing: '0.196px', fontWeight: '400' }],
        'card': ['1.3125rem', { lineHeight: '1.19', letterSpacing: '0.231px', fontWeight: '700' }],
      },
      letterSpacing: {
        'apple-body': '-0.374px',
        'apple-cap': '-0.224px',
        'apple-micro': '-0.12px',
      },
      borderRadius: {
        'xl': '0.5rem',      // 8px — Apple standard radius (overrides Tailwind default)
        '2xl': '0.75rem',    // 12px — comfortable/feature panels
        'pill': '980px',     // signature Apple pill
      },
      boxShadow: {
        'card': '0 1px 2px rgba(0,0,0,0.04)',
        'card-hover': 'rgba(0, 0, 0, 0.22) 3px 5px 30px 0px',
        'apple': 'rgba(0, 0, 0, 0.22) 3px 5px 30px 0px',
        'apple-soft': '0 2px 10px rgba(0, 0, 0, 0.06)',
      },
      backdropBlur: {
        'apple': '20px',
      },
    },
  },
  plugins: [],
}
