import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

/**
 * PrimeNG restyled for ACME People & Pay: plum primary, warm-grey surfaces, tighter radii.
 * The matching CSS custom properties for hand-built parts live in styles.scss.
 */
export const AcmePreset = definePreset(Aura, {
  primitive: {
    borderRadius: { none: '0', xs: '3px', sm: '4px', md: '6px', lg: '8px', xl: '10px' },
  },
  semantic: {
    primary: {
      50: '#f8f1f8',
      100: '#efdcef',
      200: '#dfb9df',
      300: '#c690c7',
      400: '#a7649f',
      500: '#86437f',
      600: '#6c3168',
      700: '#572654',
      800: '#441f43',
      900: '#331833',
      950: '#210e21',
    },
    focusRing: { width: '2px', style: 'solid', color: '{primary.400}', offset: '1px' },
    formField: { paddingX: '0.625rem', paddingY: '0.4375rem' },
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff',
          50: '#faf9fb',
          100: '#f4f2f6',
          200: '#e7e3ea',
          300: '#d6d0db',
          400: '#b2abb9',
          500: '#8a8492',
          600: '#6a6472',
          700: '#4d4855',
          800: '#33303a',
          900: '#1f1c24',
          950: '#141217',
        },
        primary: {
          color: '{primary.600}',
          contrastColor: '#ffffff',
          hoverColor: '{primary.700}',
          activeColor: '{primary.800}',
        },
        highlight: {
          background: '{primary.50}',
          focusBackground: '{primary.100}',
          color: '{primary.800}',
          focusColor: '{primary.900}',
        },
      },
    },
  },
});
